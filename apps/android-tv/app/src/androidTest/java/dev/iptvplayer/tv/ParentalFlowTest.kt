package dev.iptvplayer.tv

import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith

/**
 * Ebeveyn denetimi with the remote (S09, pin.md §3.1–3.3, §3.10–3.11): a PIN made with the pad's keys and the number
 * keys, changed and removed; the adult filter and Kategorileri yönet behind it; five wrong PINs and the wait; PIN'i
 * unuttum. The content locks (S10, §3.6–3.7): a locked film asks on opening, a locked category's channels are passed by
 * CH+ and ask when typed by number, and leaving the screen locks again. Each PIN check or save runs the real KDF, so
 * the waits are long. Removes this app's protection record before and after each test: a dedicated test emulator only,
 * never a TV or an emulator holding a real source. Synthetic sources and PINs.
 */
@RunWith(AndroidJUnit4::class)
class ParentalFlowTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private lateinit var server: MockWebServer
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext

    @Before fun setup() {
        // Before anything is removed: an emulator, as resetFixtureSources requires too.
        assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        ProtectionStore(context).reset()
        DeviceStore(context).hideAdultCategories = true
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val body = when (request.requestUrl!!.queryParameter("action")) {
                    null -> """{"user_info":{"auth":1,"status":"Active"}}"""
                    "get_live_categories" -> """[{"category_id":"1","category_name":"Genel"},{"category_id":"2","category_name":"Spor"}]"""
                    "get_vod_categories", "get_series_categories" -> """[{"category_id":"1","category_name":"Genel"}]"""
                    "get_live_streams" -> """[{"stream_id":1,"name":"Deneme kanalı","category_id":"1"},
                        {"stream_id":2,"name":"Spor kanalı","category_id":"2"},{"stream_id":3,"name":"Haber kanalı","category_id":"1"}]"""
                    "get_vod_streams" -> """[{"stream_id":2,"name":"Deneme filmi","category_id":"1","container_extension":"mp4"}]"""
                    "get_series" -> """[{"series_id":4,"name":"Deneme dizisi","category_id":"1"}]"""
                    else -> "[]"
                }
                return MockResponse().addHeader("Content-Type", "application/json").setBody(body)
            }
        }
        server.start()
        model = resetFixtureSources(ui)
        // MockWebServer.url looks the host name up, which the main thread may not do.
        val address = server.url("/").toString()
        ui.runOnUiThread {
            model.draft(SourceDraft("Deneme kütüphanesi", SourceType.XTREAM, address, "synthetic", "synthetic"))
            model.connectDraft()
        }
        ui.waitUntil(20_000) { model.state.value.route == Route.HOME }
        ui.waitUntil(5_000) { parental.status == ParentalStatus.OFF }
    }

    @After fun cleanup() {
        if (::model.isInitialized) ui.runOnUiThread { model.removeSource() }
        if (::server.isInitialized) {
            ProtectionStore(context).reset()
            server.shutdown()
        }
    }

    private val parental get() = model.state.value.parental
    private fun key(code: Int) { instrumentation.sendKeyDownUpSync(code); ui.waitForIdle() }
    /** The remote's number keys. */
    private fun digits(pin: String) = pin.forEach { key(KeyEvent.KEYCODE_0 + (it - '0')) }
    private fun row(label: String) = ui.onNode(hasText(label) and hasClickAction() and hasAnyAncestor(hasTestTag("settings-panel")))
    private fun title(text: String) = ui.onNodeWithTag("settings-title").assertTextEquals(text)
    private fun focused(tag: String) = ui.onAllNodes(hasTestTag(tag) and isFocused()).fetchSemanticsNodes().isNotEmpty()
    private fun padShown() = ui.onAllNodesWithTag("pin-pad").fetchSemanticsNodes().isNotEmpty()
    private fun padTitle(text: String) = ui.onNode(hasText(text) and hasAnyAncestor(hasTestTag("source-dialog"))).assertExists()
    /** The pad's feedback line, as TalkBack reads it: one node whose description follows each message. */
    private fun feedback(text: String) = ui.onNodeWithTag("pin-feedback").assertContentDescriptionEquals(text)
    /** A PIN check or save: seconds of KDF on an emulator. */
    private fun waitFor(condition: () -> Boolean) = ui.waitUntil(60_000, condition)

    private fun openSettings() {
        ui.onNodeWithTag("nav:Ayarlar").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        // A platform Dialog acquires window focus independently of Compose's idle clock.
        ui.waitUntil(5_000) { ui.onAllNodes(hasText("Hareketi azalt") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
    }

    /** Opens the pad with OK on the focused row; focus starts on its 1 key. */
    private fun openPad() {
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { padShown() && focused("pin-key:1") }
    }

    /** PIN 2580 through the model, as Ebeveyn denetimi would make it; the grant of making it ends at once. */
    private fun makePin() {
        ui.runOnUiThread {
            assertTrue(model.createPin())
            model.submitPin("2580".toCharArray()); model.submitPin("2580".toCharArray())
        }
        waitFor { parental.status == ParentalStatus.ON && parental.prompt == null }
        ui.runOnUiThread { model.closeSettings() }
    }

    /**
     * Puts [rules] in the protection record beside the PIN, as Kilitle will from the screens (S11), and opens the
     * library again, which reads them first.
     */
    private fun lock(vararg rules: AccessRule) {
        val store = ProtectionStore(context)
        val current = (store.read() as ProtectionRead.Ready).snapshot
        store.write(ProtectionSnapshot(current.generation + 1, current.policyRevision + 1, current.credential, current.attempts,
            current.rules + rules, current.options), current.generation)
        ui.runOnUiThread { model.restore() }
        ui.waitUntil(20_000) { model.state.value.route == Route.HOME && model.state.value.sourceTask == SourceTask.IDLE }
    }
    private fun entry(id: String) = model.state.value.allCatalog.items.single { it.id == id }
    private fun card(id: String) = ui.onNode(hasClickAction() and hasAnyAncestor(hasTestTag("tile:library:$id")))
    private val playingId get() = model.state.value.playing?.entry?.id

    @Test fun aPinIsMadeChangedAndRemovedWithTheRemote() {
        openSettings()
        row("Ebeveyn denetimi").performSemanticsAction(SemanticsActions.RequestFocus)
        ui.onNodeWithTag("settings-section").assertTextEquals("İçerik")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Ebeveyn denetimi")
        row("PIN oluştur").assertIsFocused()
        openPad()
        padTitle("PIN oluştur")
        // An easy PIN is refused.
        digits("1111")
        feedback("Bu PIN kolay tahmin edilir. Farklı bir PIN seç.")
        ui.onNodeWithTag("pin-boxes").assertContentDescriptionEquals("0 / 4 rakam girildi")
        // The pad's keys with the D-pad: 2, 5, 8 down the middle; Sil takes the 8 back; then 8 and 0.
        key(KeyEvent.KEYCODE_DPAD_RIGHT); key(KeyEvent.KEYCODE_DPAD_CENTER)
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_CENTER)
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("pin-boxes").assertContentDescriptionEquals("3 / 4 rakam girildi")
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithTag("pin-key:delete").assertIsFocused().assertContentDescriptionEquals("Son rakamı sil")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("pin-boxes").assertContentDescriptionEquals("2 / 4 rakam girildi")
        key(KeyEvent.KEYCODE_DPAD_UP); key(KeyEvent.KEYCODE_DPAD_LEFT)
        ui.onNodeWithTag("pin-key:8").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_CENTER)
        padTitle("PIN'i tekrar gir")
        assertEquals(PinStep.CONFIRM, parental.prompt?.step)
        // The repeat with the number keys.
        digits("2580")
        waitFor { parental.status == ParentalStatus.ON && parental.prompt == null }
        ui.waitUntil(5_000) { !padShown() }
        title("Ebeveyn denetimi")
        ui.onNodeWithText("PIN oluşturuldu.", substring = true).assertExists()
        ui.waitUntil(5_000) { focused("parental-change") }
        assertTrue(parental.adminUnlocked)

        // A new PIN: Back on the repeat returns to the first step.
        openPad()
        padTitle("Yeni PIN")
        digits("1397")
        padTitle("PIN'i tekrar gir")
        key(KeyEvent.KEYCODE_BACK)
        padTitle("Yeni PIN")
        assertEquals(PinStep.NEW, parental.prompt?.step)
        digits("1397"); digits("1397")
        waitFor { parental.prompt == null }
        ui.waitUntil(5_000) { !padShown() && focused("parental-change") }
        ui.onNodeWithText("PIN değiştirildi.").assertExists()

        // Removing it asks first, Vazgeç focused.
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("parental-remove").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("PIN kaldırılsın mı?")
        row("Vazgeç").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("parental-remove-confirm").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        waitFor { parental.status == ParentalStatus.OFF }
        title("Ebeveyn denetimi")
        ui.waitUntil(5_000) { focused("parental-create") }
        ui.onNodeWithText("PIN kaldırıldı.").assertExists()
        assertEquals(ProtectionRead.Unconfigured, ProtectionStore(context).read())
        assertTrue(model.state.value.hideAdultCategories)
        key(KeyEvent.KEYCODE_BACK)
        title("Ayarlar")
        row("Ebeveyn denetimi").assertIsFocused()
    }

    @Test fun theAdultFilterAndKategorileriYonetAskForThePin() {
        openSettings()
        val filter = row("Yetişkin kategorilerini gizle")
        filter.performSemanticsAction(SemanticsActions.RequestFocus)
        // Without a PIN, turning it off first offers one; Vazgeç changes nothing.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Önce PIN oluşturmak ister misin?")
        ui.waitUntil(5_000) { focused("offer-create-pin") }
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_DOWN)
        row("Vazgeç").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Ayarlar")
        filter.assertIsFocused().assertIsOn()
        // PIN oluştur: the filter goes off once the new PIN is saved.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { focused("offer-create-pin") }
        openPad()
        digits("2580"); digits("2580")
        waitFor { parental.status == ParentalStatus.ON && !model.state.value.hideAdultCategories }
        ui.waitUntil(5_000) { !padShown() }
        title("Ayarlar")
        ui.waitUntil(5_000) { ui.onAllNodes(hasText("Yetişkin kategorilerini gizle") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
        filter.assertIsOff()
        // On is free, and off again asks nothing more in the same visit.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        assertTrue(model.state.value.hideAdultCategories)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        assertFalse(model.state.value.hideAdultCategories); assertFalse(padShown())
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)
        assertFalse(parental.adminUnlocked)

        // A new visit asks again; Back on the pad leaves the filter on and focus on its row.
        openSettings()
        filter.performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        padTitle("PIN gir")
        ui.onNodeWithText("Yetişkin kategorilerini göstermek için PIN gir.").assertExists()
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { !padShown() }
        filter.assertIsFocused().assertIsOn()
        assertTrue(model.state.value.hideAdultCategories)
        // Kategorileri yönet asks too, and opens with the right PIN.
        row("Kategorileri yönet").performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        ui.onNodeWithText("Kategori ayarlarını açmak için PIN gir.").assertExists()
        digits("2580")
        waitFor { parental.prompt == null }
        ui.waitUntil(5_000) { !padShown() && focused("category-kind:LIVE") }
        title("Kategoriler")
        key(KeyEvent.KEYCODE_BACK)
        row("Kategorileri yönet").assertIsFocused()
    }

    @Test fun fiveWrongPinsMakeThePadWait() {
        makePin()
        openSettings()
        row("Ebeveyn denetimi").performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        padTitle("PIN gir")
        ui.onNodeWithText("Ebeveyn denetimi ayarlarını açmak için PIN gir.").assertExists()
        fun wrong() {
            digits("1111")
            waitFor { parental.prompt?.phase.let { it == PinPhase.Entry || it is PinPhase.Waiting } }
        }
        repeat(3) { wrong(); feedback("PIN yanlış. Tekrar dene.") }
        wrong()
        feedback("PIN yanlış. Bir yanlış deneme daha 30 saniye bekletir.")
        wrong()
        assertTrue(parental.prompt?.phase is PinPhase.Waiting)
        // The 30 s start when the fifth try is charged, before the slow PIN check (pin.md: the reservation comes first),
        // so the pad reads what is left of them as the wait begins.
        val spoken = ui.onNodeWithTag("pin-feedback").fetchSemanticsNode().config[SemanticsProperties.ContentDescription].single()
        val left = Regex("""Çok fazla yanlış deneme\. (\d+) saniye sonra tekrar deneyebilirsin\.""").matchEntire(spoken)?.groupValues?.get(1)?.toInt()
        assertTrue("The wait reads what is left of 30 s: $spoken", left != null && left in 20..30)
        ui.onNodeWithTag("pin-key:1").assertIsNotEnabled()
        // The number keys take nothing while it lasts, not even the right PIN.
        digits("2580")
        assertTrue(parental.prompt?.phase is PinPhase.Waiting)
        assertFalse(parental.adminUnlocked)
        // Back grants nothing: the page stays closed and focus is back on its row.
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { !padShown() }
        title("Ayarlar")
        row("Ebeveyn denetimi").assertIsFocused()
        // Opened again, the pad is still waiting and starts on Vazgeç.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { padShown() && focused("pin-cancel") }
        assertTrue(parental.prompt?.phase is PinPhase.Waiting)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { !padShown() }
        row("Ebeveyn denetimi").assertIsFocused()
        assertNull(parental.prompt)
    }

    @Test fun aLockedFilmAsksForThePinOnOpeningAndThePinOpensIt() {
        makePin()
        val source = model.state.value.sources.single().id
        val film = model.fixtureKey("movie:2")
        lock(AccessRule(AccessScope.Content(ContentRef(source, "movie:2")), pinRequired = true))
        assertEquals(setOf(film), model.state.value.locks.ids)
        // A locked film is listed and opened from the library; OK asks for the PIN, focus on its 1 key.
        ui.runOnUiThread { model.showKind(MediaKind.MOVIE) }
        ui.waitUntil(5_000) { ui.onAllNodes(hasClickAction() and hasAnyAncestor(hasTestTag("tile:library:$film"))).fetchSemanticsNodes().isNotEmpty() }
        card(film).performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        padTitle("PIN gir")
        ui.onNodeWithText("“Deneme filmi” kilitli. Açmak için PIN gir.").assertExists()
        // Back opens nothing, and focus is back on the card.
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { !padShown() }
        assertEquals(Route.LIBRARY, model.state.value.route)
        ui.waitUntil(5_000) { ui.onAllNodes(isFocused() and hasAnyAncestor(hasTestTag("tile:library:$film"))).fetchSemanticsNodes().isNotEmpty() }
        // The right PIN opens its detail, and opens nothing in Ayarlar.
        openPad()
        digits("2580")
        waitFor { model.state.value.route == Route.DETAIL }
        assertEquals(film, model.state.value.selected?.id)
        assertEquals(1, parental.sessionGrants); assertFalse(parental.adminUnlocked)
        // Opened for the session: no more PIN until the app leaves the screen.
        ui.runOnUiThread { model.back(); assertFalse(model.open(entry(film))) }
        assertEquals(Route.DETAIL, model.state.value.route)
        ui.runOnUiThread { model.back(); model.appStopped() }
        assertEquals(0, parental.sessionGrants)
        ui.runOnUiThread { assertTrue(model.open(entry(film))) }
        assertEquals(Route.LIBRARY, model.state.value.route)
    }

    @Test fun aLockedCategorysChannelsArePassedByChannelUpAndAskWhenTypedByNumber() {
        makePin()
        val source = model.state.value.sources.single().id
        val (first, sport, news) = listOf("live:1", "live:2", "live:3").map { model.fixtureKey(it) }
        lock(AccessRule(AccessScope.Category(source, MediaKind.LIVE, AccessCategory.ProviderId("2")), pinRequired = true))
        assertEquals(setOf(sport), model.state.value.locks.ids)
        assertEquals(setOf(CategoryVisibility.key(MediaKind.LIVE, "Spor")), model.state.value.locks.categories)
        ui.runOnUiThread { model.showKind(MediaKind.LIVE); assertFalse(model.open(entry(first))) }
        ui.waitUntil(5_000) { model.state.value.route == Route.PLAYER && playingId == first }
        // The model gets there a frame before the player is on screen and has focus, which the remote's keys need.
        ui.waitForIdle()
        // CH+ never asks: it passes the locked channel.
        key(KeyEvent.KEYCODE_CHANNEL_UP)
        ui.waitUntil(5_000) { playingId == news }
        // Its number asks over the picture; the channel playing goes on behind the pad, and after Back.
        val number = model.channelNumber(entry(sport))!!
        key(KeyEvent.KEYCODE_0 + number)
        ui.waitUntil(5_000) { padShown() }
        ui.onNodeWithText("“Spor kanalı” kilitli. Açmak için PIN gir.").assertExists()
        assertEquals(news, playingId)
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { !padShown() && ui.activity.hasWindowFocus() }
        assertEquals(news, playingId)
        key(KeyEvent.KEYCODE_0 + number)
        ui.waitUntil(5_000) { padShown() }
        digits("2580")
        waitFor { playingId == sport }
        // The pad's window keeps the system's input focus for a moment after it closes; keys sent before the player's
        // window has it again never reach the app.
        ui.waitUntil(5_000) { !padShown() && ui.activity.hasWindowFocus() }
        ui.waitForIdle()
        // Opened for the session, CH± stops on it again.
        key(KeyEvent.KEYCODE_CHANNEL_UP)
        ui.waitUntil(5_000) { playingId == news }
        key(KeyEvent.KEYCODE_CHANNEL_DOWN)
        ui.waitUntil(5_000) { playingId == sport }
        ui.runOnUiThread { model.back() }
    }

    @Test fun forgettingThePinStartsTheTvOver() {
        makePin()
        openSettings()
        row("Kategorileri yönet").performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        ui.onNodeWithTag("pin-forgot").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { focused("pin-forgot-cancel") }
        // Vazgeç first, back on PIN'i unuttum; nothing is removed.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { focused("pin-forgot") }
        assertEquals(1, model.state.value.sources.size)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { focused("pin-forgot-cancel") }
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("pin-forgot-reset").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        waitFor { model.state.value.route == Route.SETUP && parental.status == ParentalStatus.OFF }
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)
        assertFalse(padShown())
        assertTrue(model.state.value.sources.isEmpty())
        assertTrue(model.state.value.hideAdultCategories)
        ui.onNodeWithTag("setup-notice").assertTextEquals("Sıfırlandı. Kaynağını yeniden ekleyebilirsin.")
        assertEquals(ProtectionRead.Unconfigured, ProtectionStore(context).read())
    }
}
