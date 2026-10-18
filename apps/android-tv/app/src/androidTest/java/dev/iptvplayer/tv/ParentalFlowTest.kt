package dev.iptvplayer.tv

import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Ebeveyn denetimi with the remote (S09, pin.md §3.1–3.3, §3.10–3.11): a PIN made with the pad's keys and the number
 * keys, changed and removed; the adult filter and Kategorileri yönet behind it; five wrong PINs and the wait; PIN'i
 * unuttum. The content locks (S10, §3.6–3.7): a locked film asks on opening, a locked category's channels are passed by
 * CH+ and ask when typed by number, and leaving the screen locks again. The lock screens (S11, §3.4–3.5, §3.9, §4):
 * Göster / Kilitle / Gizle in Kategorileri yönet (Kilitle making a first PIN, a locked row hidden and shown again), the
 * marks and hidden programmes in the library, guide and Ara, Kilitle and Kilidi kaldır in the quick menu, Kilitli
 * içerikler, the 1024-rule limit, and, as the app stops, a locked title leaving the player and its programme detail
 * leaving the guide. Source and launch protection (S12, §3.8–3.9): Kaynakları yönet, Kaynağı düzenle and Kaynağı kaldır
 * asking for the PIN from Ayarlar and the guide and the PIN holding in Kaynaklar, a whole source locked from its
 * Bilgiler, "Açılışta PIN iste" on return over the player and at launch, and the lock screen of a record that cannot be
 * read. Each PIN check or save runs the real KDF, so the waits are long. Removes this app's protection record before and
 * after each test: a dedicated test emulator only, never a TV or an emulator holding a real source. Synthetic sources
 * and PINs.
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
                // The source's guide: a locked channel must keep its programme to itself.
                if (request.requestUrl!!.encodedPath.endsWith("xmltv.php"))
                    return MockResponse().addHeader("Content-Type", "application/xml").setBody(guide())
                val body = when (request.requestUrl!!.queryParameter("action")) {
                    null -> """{"user_info":{"auth":1,"status":"Active"}}"""
                    "get_live_categories" -> """[{"category_id":"1","category_name":"Genel"},{"category_id":"2","category_name":"Spor"}]"""
                    "get_vod_categories", "get_series_categories" -> """[{"category_id":"1","category_name":"Genel"}]"""
                    "get_live_streams" -> """[{"stream_id":1,"name":"Deneme kanalı","category_id":"1","epg_channel_id":"genel"},
                        {"stream_id":2,"name":"Spor kanalı","category_id":"2","epg_channel_id":"spor"},
                        {"stream_id":3,"name":"Haber kanalı","category_id":"1","epg_channel_id":"haber"}]"""
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
        if (::server.isInitialized) {
            // The PIN goes first: while it guards the sources, removing one would ask for it.
            ProtectionStore(context).reset()
            if (::model.isInitialized) {
                ui.runOnUiThread { model.retryProtection() }
                ui.waitUntil(5_000) { parental.status == ParentalStatus.OFF }
                ui.runOnUiThread { model.removeSource() }
            }
            server.shutdown()
        }
    }

    /** One programme on each channel now. */
    private fun guide(): String {
        val now = System.currentTimeMillis()
        fun time(at: Long) = Instant.ofEpochMilli(at).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("uuuuMMddHHmmss xx", Locale.ROOT))
        val channels = listOf("genel" to "Genel program", "spor" to "Spor programı", "haber" to "Haber programı")
        return "<tv>" + channels.joinToString("") { (id, _) -> "<channel id='$id'><display-name>$id</display-name></channel>" } +
            channels.joinToString("") { (id, title) ->
                "<programme channel='$id' start='${time(now - 3_600_000)}' stop='${time(now + 3_600_000)}'><title>$title</title></programme>"
            } + "</tv>"
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
     * Puts [rules] in the protection record beside the PIN, and [options] in place of its own, and opens the library
     * again, which reads them first.
     */
    private fun lock(vararg rules: AccessRule, options: ProtectionOptions? = null) {
        val store = ProtectionStore(context)
        val current = (store.read() as ProtectionRead.Ready).snapshot
        store.write(ProtectionSnapshot(current.generation + 1, current.policyRevision + 1, current.credential, current.attempts,
            current.rules + rules, options ?: current.options), current.generation)
        ui.runOnUiThread { model.restore() }
        ui.waitUntil(20_000) { model.state.value.route == Route.HOME && model.state.value.sourceTask == SourceTask.IDLE }
    }
    private fun entry(id: String) = model.state.value.allCatalog.items.single { it.id == id }
    private fun card(id: String) = ui.onNode(hasClickAction() and hasAnyAncestor(hasTestTag("tile:library:$id")))
    private fun cardShown(id: String) = ui.onAllNodes(hasClickAction() and hasAnyAncestor(hasTestTag("tile:library:$id"))).fetchSemanticsNodes().isNotEmpty()
    private val playingId get() = model.state.value.playing?.entry?.id
    /** TalkBack's state of a locked card or row (pin.md §4). */
    private val readsLocked = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Kilitli")
    private fun state(text: String) = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, text)
    /** A Kilitli içerikler row, whose OK TalkBack names. */
    private val okRemovesTheLock = SemanticsMatcher("OK reads Kilidi kaldır") { it.config.getOrNull(SemanticsActions.OnClick)?.label == "Kilidi kaldır" }
    private fun rules() = (ProtectionStore(context).read() as ProtectionRead.Ready).snapshot.rules
    private fun shown(text: String) = ui.onAllNodes(hasText(text, substring = true), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun lockShown() = ui.onAllNodesWithTag("app-lock").fetchSemanticsNodes().isNotEmpty()
    private fun rowFocused(label: String) = ui.onAllNodes(hasText(label) and hasClickAction() and isFocused() and
        hasAnyAncestor(hasTestTag("settings-panel"))).fetchSemanticsNodes().isNotEmpty()

    /** Kaynaklar through Ayarlar → Kaynakları yönet, with the PIN that guards them. */
    private fun openSourcesWithPin() {
        openSettings()
        row("Kaynakları yönet").performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        digits("2580")
        waitFor { model.state.value.route == Route.SOURCES }
        ui.waitUntil(5_000) { !padShown() && focused("add-source") }
    }

    /** Ebeveyn denetimi with the PIN, focus on its first row. */
    private fun openParentalPage() {
        openSettings()
        row("Ebeveyn denetimi").performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        digits("2580")
        waitFor { parental.prompt == null }
        ui.waitUntil(5_000) { !padShown() && focused("parental-change") }
    }

    /** Opens the quick menu with a held OK on [id]'s card, focus on its first row. */
    private fun quickMenu(id: String) {
        card(id).performSemanticsAction(SemanticsActions.RequestFocus)
        card(id).performSemanticsAction(SemanticsActions.OnLongClick)
        ui.waitUntil(5_000) { focused("quick-favorite") }
    }

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

        // Removing it asks first, Vazgeç focused; Kilitli içerikler sits between, with nothing locked yet, then the two
        // options: source management asks by default, the app's opening does not.
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNode(hasTestTag("parental-locked") and hasText("Kilitli kanal veya içerik yok.")).assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("parental-sources-need-pin").assertIsFocused().assertIsOn()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("parental-launch-needs-pin").assertIsFocused().assertIsOff()
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

    @Test fun kategorileriYonetLocksARowWhoseChannelsShowTheLockAndKeepTheirProgrammesUntilThePin() {
        makePin()
        val source = model.state.value.sources.single().id
        val (first, sport) = listOf("live:1", "live:2").map { model.fixtureKey(it) }
        openSettings()
        row("Kategorileri yönet").performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        digits("2580")
        waitFor { parental.prompt == null }
        ui.waitUntil(5_000) { !padShown() && focused("category-kind:LIVE") }
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Canlı TV")
        // Each row says how it stands; OK offers Göster, Kilitle and Gizle, the current one first and checked.
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("category-choice:1").assertIsFocused().assert(state("Açık"))
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Spor")
        ui.onNodeWithTag("settings-option:0").assertIsFocused().assertIsSelected()
        ui.onNode(hasTestTag("settings-option:1") and hasText("Kilitle · PIN ister")).assertExists()
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Canlı TV")
        ui.waitUntil(10_000) { model.state.value.locks.categories == setOf(CategoryVisibility.key(MediaKind.LIVE, "Spor")) }
        ui.onNodeWithTag("category-choice:1").assertIsFocused().assert(readsLocked)
        assertTrue((ProtectionStore(context).read() as ProtectionRead.Ready).snapshot.rules.contains(
            AccessRule(AccessScope.Category(source, MediaKind.LIVE, AccessCategory.ProviderId("2")), pinRequired = true)))
        assertEquals(setOf(sport), model.state.value.locks.ids)
        key(KeyEvent.KEYCODE_BACK); key(KeyEvent.KEYCODE_BACK); key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)

        // Canlı TV: the row reads "Kilitli" and its category "Spor, kilitli", still selectable as shown or not; its
        // programme stays hidden, the others' show.
        ui.runOnUiThread { model.showKind(MediaKind.LIVE) }
        ui.waitUntil(20_000) { cardShown(sport) && shown("Genel program") }
        card(sport).assert(readsLocked)
        ui.onNode(hasText("Kilitli kanal") and hasAnyAncestor(hasTestTag("tile:library:$sport"))).assertExists()
        card(first).assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))
        ui.onNodeWithTag("category:Spor").assert(hasContentDescription("Spor, kilitli"))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))
        assertFalse(shown("Spor programı"))
        // The guide: one "Kilitli kanal" cell across the locked row.
        ui.runOnUiThread { model.guide() }
        ui.waitUntil(10_000) { ui.onAllNodesWithTag("guide-row:$sport").fetchSemanticsNodes().isNotEmpty() && shown("Genel program") }
        ui.onNode(hasText("Kilitli kanal") and hasAnyAncestor(hasTestTag("guide-row:$sport"))).assertExists()
        assertFalse(shown("Spor programı"))
        // Ara: the locked result reads "Kilitli" too.
        ui.runOnUiThread { model.search(); model.searchQuery("kanal") }
        ui.waitUntil(10_000) { ui.onAllNodesWithTag("search:$sport").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithTag("search:$sport").assert(readsLocked)
        ui.onNodeWithTag("search:$first").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))

        // The PIN opens it for the session: no mark and its programme, until the app leaves the screen.
        ui.runOnUiThread { model.showKind(MediaKind.LIVE) }
        ui.waitUntil(5_000) { cardShown(sport) }
        card(sport).performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        digits("2580")
        waitFor { model.state.value.route == Route.PLAYER && playingId == sport }
        ui.waitUntil(5_000) { !padShown() && ui.activity.hasWindowFocus() }
        ui.runOnUiThread { model.back() }
        ui.waitUntil(10_000) { cardShown(sport) && shown("Spor programı") }
        card(sport).assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))
        ui.runOnUiThread { model.appStopped() }
        ui.waitUntil(5_000) { !shown("Spor programı") }
        card(sport).assert(readsLocked)

        // Göster removes the row's lock, with the PIN verified for Kategorileri yönet.
        openSettings()
        row("Kategorileri yönet").performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        digits("2580")
        waitFor { parental.prompt == null }
        ui.waitUntil(5_000) { !padShown() && focused("category-kind:LIVE") }
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Spor")
        ui.onNodeWithTag("settings-option:1").assertIsFocused().assertIsSelected()
        key(KeyEvent.KEYCODE_DPAD_UP); key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { model.state.value.locks.categories.isEmpty() }
        ui.onNodeWithTag("category-choice:1").assertIsFocused().assert(state("Açık"))
        assertTrue((ProtectionStore(context).read() as ProtectionRead.Ready).snapshot.rules.isEmpty())
        assertTrue(model.state.value.locks.ids.isEmpty())
    }

    @Test fun theQuickMenuLocksATitleAndKilidiKaldirAsksForThePin() {
        makePin()
        val film = model.fixtureKey("movie:2")
        ui.runOnUiThread { model.showKind(MediaKind.MOVIE) }
        ui.waitUntil(5_000) { cardShown(film) }
        // Kilitle: at once, without the PIN; focus goes back to the card, which reads "Kilitli".
        quickMenu(film)
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("quick-lock").assertIsFocused().assertTextEquals("Kilitle")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { film in model.state.value.locks.contents && film in model.state.value.locks.ids }
        ui.waitUntil(5_000) { ui.onAllNodes(isFocused() and hasAnyAncestor(hasTestTag("tile:library:$film"))).fetchSemanticsNodes().isNotEmpty() }
        card(film).assert(readsLocked)
        assertFalse(padShown())
        // Kilidi kaldır asks for the PIN; Back keeps the lock and focus on the card.
        quickMenu(film)
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("quick-lock").assertIsFocused().assertTextEquals("Kilidi kaldır")
        openPad()
        ui.onNodeWithText("“Deneme filmi” kilidini kaldırmak için PIN gir.").assertExists()
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { !padShown() }
        ui.waitUntil(5_000) { ui.onAllNodes(isFocused() and hasAnyAncestor(hasTestTag("tile:library:$film"))).fetchSemanticsNodes().isNotEmpty() }
        assertTrue(film in model.state.value.locks.contents)
        // The right PIN removes that lock and opens nothing else.
        quickMenu(film)
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_DOWN)
        openPad()
        digits("2580")
        waitFor { film !in model.state.value.locks.contents }
        assertTrue(model.state.value.locks.ids.isEmpty())
        assertFalse(parental.adminUnlocked); assertEquals(0, parental.sessionGrants)
        assertEquals(Route.LIBRARY, model.state.value.route)
        ui.waitUntil(5_000) { !padShown() }
        card(film).assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))

        // With the record full, Kilitle says so in the corner (pin.md §5).
        val source = model.state.value.sources.single().id
        lock(*Array(1024) { AccessRule(AccessScope.Content(ContentRef(source, "movie:${it + 100}")), pinRequired = true) })
        ui.runOnUiThread { model.showKind(MediaKind.MOVIE) }
        ui.waitUntil(5_000) { cardShown(film) }
        quickMenu(film)
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_DOWN)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { ui.onAllNodesWithTag("access-note").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithTag("access-note").assertTextEquals("Kilit sınırına ulaşıldı. Bazı kilitleri kaldır.")
        assertFalse(film in model.state.value.locks.contents)
    }

    @Test fun kilitliIceriklerListsTheLocksAndOkRemovesThem() {
        makePin()
        val source = model.state.value.sources.single().id
        val film = model.fixtureKey("movie:2")
        lock(AccessRule(AccessScope.Category(source, MediaKind.LIVE, AccessCategory.ProviderId("2")), pinRequired = true),
            AccessRule(AccessScope.Content(ContentRef(source, "movie:2")), pinRequired = true))
        openSettings()
        row("Ebeveyn denetimi").performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        digits("2580")
        waitFor { parental.prompt == null }
        ui.waitUntil(5_000) { !padShown() && focused("parental-change") }
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("parental-locked").assertIsFocused()
        ui.onNode(hasTestTag("parental-locked") and hasText("2 kilit")).assertExists()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Kilitli içerikler")
        // Under their headings: the category row, then the title, each reading "Kilitli" with OK as "Kilidi kaldır";
        // OK removes a lock and focus moves on.
        val category = "locked-category:LIVE:Spor"
        ui.onNodeWithTag(category).assertIsFocused().assert(readsLocked).assert(okRemovesTheLock)
        ui.onNode(hasTestTag("locked-title:$film") and hasText("Deneme filmi")).assert(readsLocked).assert(okRemovesTheLock)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { model.state.value.locks.categories.isEmpty() }
        ui.onNodeWithTag("locked-title:$film").assertIsFocused()
        ui.onAllNodesWithTag(category).assertCountEquals(0)
        // With the last one gone the page leaves, focus back on its row.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { model.state.value.locks.contents.isEmpty() }
        title("Ebeveyn denetimi")
        ui.waitUntil(5_000) { focused("parental-locked") }
        ui.onNode(hasTestTag("parental-locked") and hasText("Kilitli kanal veya içerik yok.")).assertExists()
        assertTrue((ProtectionStore(context).read() as ProtectionRead.Ready).snapshot.rules.isEmpty())
    }

    @Test fun aLockedTitleInThePlayerLeavesItWhenTheAppLeavesTheScreen() {
        makePin()
        val source = model.state.value.sources.single().id
        val (first, sport) = listOf("live:1", "live:2").map { model.fixtureKey(it) }
        lock(AccessRule(AccessScope.Content(ContentRef(source, "live:2")), pinRequired = true))
        ui.runOnUiThread { model.showKind(MediaKind.LIVE) }
        ui.waitUntil(5_000) { cardShown(sport) }
        card(sport).performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        digits("2580")
        waitFor { model.state.value.route == Route.PLAYER && playingId == sport }
        ui.waitUntil(5_000) { !padShown() && ui.activity.hasWindowFocus() }
        // Home, standby or another app: back on screen the library shows, and the channel asks again.
        ui.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        assertEquals(Route.LIBRARY, model.state.value.route)
        assertEquals(0, parental.sessionGrants)
        ui.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        ui.waitUntil(5_000) { cardShown(sport) }
        card(sport).assert(readsLocked)
        ui.runOnUiThread { assertTrue(model.open(entry(sport))) }
        assertEquals(Route.LIBRARY, model.state.value.route)
        // A channel without a lock plays on.
        ui.runOnUiThread { assertFalse(model.open(entry(first))) }
        ui.waitUntil(5_000) { model.state.value.route == Route.PLAYER && playingId == first }
        ui.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        assertEquals(Route.PLAYER, model.state.value.route)
        ui.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        ui.runOnUiThread { model.back() }
    }

    @Test fun kilitleOnATvWithoutAPinMakesOneAndLocksTheRow() {
        val source = model.state.value.sources.single().id
        openSettings()
        // Without a PIN, Kategorileri yönet opens at once.
        row("Kategorileri yönet").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { focused("category-kind:LIVE") }
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Canlı TV")
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("category-choice:1").assertIsFocused().assert(state("Açık"))
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Spor")
        ui.onNode(hasTestTag("settings-option:1") and hasText("Kilitle · önce PIN oluştur")).assertExists()
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_CENTER)
        // The pad makes the first PIN over the list; the row locks once it is saved, focus back on it.
        ui.waitUntil(5_000) { padShown() && focused("pin-key:1") }
        padTitle("PIN oluştur")
        digits("2580"); digits("2580")
        waitFor { parental.status == ParentalStatus.ON && model.state.value.locks.categories == setOf(CategoryVisibility.key(MediaKind.LIVE, "Spor")) }
        ui.waitUntil(5_000) { !padShown() }
        title("Canlı TV")
        ui.waitUntil(5_000) { focused("category-choice:1") }
        ui.onNodeWithTag("category-choice:1").assert(readsLocked)
        ui.onNodeWithTag("settings-notice").assertTextContains("PIN oluşturuldu.", substring = true)
        assertTrue(rules().contains(AccessRule(AccessScope.Category(source, MediaKind.LIVE, AccessCategory.ProviderId("2")), pinRequired = true)))
        // The note belongs to that change: once another page has opened, the list shows it no more.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Spor")
        key(KeyEvent.KEYCODE_BACK)
        title("Canlı TV")
        ui.onAllNodesWithTag("settings-notice").assertCountEquals(0)
    }

    @Test fun aLockedRowHiddenInKategorileriYonetKeepsItsLockOutOfSightAndKilitleBringsItBackLocked() {
        makePin()
        val source = model.state.value.sources.single().id
        val sport = model.fixtureKey("live:2")
        val rule = AccessRule(AccessScope.Category(source, MediaKind.LIVE, AccessCategory.ProviderId("2")), pinRequired = true)
        val spor = CategoryVisibility.key(MediaKind.LIVE, "Spor")
        fun hidden() = spor in model.state.value.hiddenCategories && model.state.value.allCatalog.items.none { it.id == sport }
        /** Picks option [index] of Spor's choices, from the one checked now. */
        fun choose(index: Int) {
            key(KeyEvent.KEYCODE_DPAD_CENTER)
            title("Spor")
            val checked = model.state.value.categoryAccess(MediaKind.LIVE, "Spor").ordinal
            ui.onNodeWithTag("settings-option:$checked").assertIsFocused().assertIsSelected()
            repeat(Math.abs(index - checked)) { key(if (index > checked) KeyEvent.KEYCODE_DPAD_DOWN else KeyEvent.KEYCODE_DPAD_UP) }
            key(KeyEvent.KEYCODE_DPAD_CENTER)
            title("Canlı TV")
        }
        lock(rule)
        openSettings()
        row("Kategorileri yönet").performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        digits("2580")
        waitFor { parental.prompt == null }
        ui.waitUntil(5_000) { !padShown() && focused("category-kind:LIVE") }
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Canlı TV")
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("category-choice:1").assertIsFocused().assert(readsLocked)
        // Gizle: the row leaves the library, and its lock stays in the record under the hide.
        choose(CategoryAccess.HIDDEN.ordinal)
        ui.waitUntil(10_000) { hidden() }
        ui.onNodeWithTag("category-choice:1").assertIsFocused().assert(state("Gizli"))
        assertTrue(rules().contains(rule))
        // Kilitli içerikler neither lists nor counts it: the row reads as hidden alone, as it will after a restart.
        assertTrue(model.state.value.locks.categories.isEmpty())
        key(KeyEvent.KEYCODE_BACK); key(KeyEvent.KEYCODE_BACK)
        title("Ayarlar")
        row("Ebeveyn denetimi").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { focused("parental-change") }
        ui.onNode(hasTestTag("parental-locked") and hasText("Kilitli kanal veya içerik yok.")).assertExists()
        key(KeyEvent.KEYCODE_BACK)
        row("Kategorileri yönet").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { focused("category-kind:LIVE") }
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { focused("category-choice:1") }
        // Göster: the lock kept under the hide goes with it, and the row shows unlocked.
        choose(CategoryAccess.SHOWN.ordinal)
        ui.waitUntil(10_000) { spor !in model.state.value.hiddenCategories && model.state.value.allCatalog.items.any { it.id == sport } }
        ui.onNodeWithTag("category-choice:1").assertIsFocused().assert(state("Açık"))
        assertTrue(rules().isEmpty())
        assertTrue(model.state.value.locks.ids.isEmpty())
        // Hidden without a lock, Kilitle brings it back locked: the new rule is saved before the row shows, and the two
        // publishes that follow (the rule, then the row shown) never leave the older library over the newer.
        choose(CategoryAccess.HIDDEN.ordinal)
        ui.waitUntil(10_000) { hidden() }
        choose(CategoryAccess.LOCKED.ordinal)
        ui.waitUntil(10_000) { model.state.value.locks.categories == setOf(spor) && spor !in model.state.value.hiddenCategories }
        Thread.sleep(1_000); ui.waitForIdle()
        assertTrue(spor !in model.state.value.hiddenCategories && spor !in DeviceStore(context).hiddenCategories)
        assertEquals(setOf(spor), model.state.value.locks.categories)
        assertEquals(setOf(sport), model.state.value.locks.ids)
        assertTrue(rules().contains(rule))
        ui.onNodeWithTag("category-choice:1").assertIsFocused().assert(readsLocked)
    }

    @Test fun aPinOpenedChannelsProgrammeDetailClosesWhenTheLockComesBack() {
        makePin()
        val source = model.state.value.sources.single().id
        val sport = model.fixtureKey("live:2")
        lock(AccessRule(AccessScope.Content(ContentRef(source, "live:2")), pinRequired = true))
        ui.runOnUiThread { model.showKind(MediaKind.LIVE) }
        ui.waitUntil(5_000) { cardShown(sport) }
        card(sport).performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        digits("2580")
        waitFor { model.state.value.route == Route.PLAYER && playingId == sport }
        ui.waitUntil(5_000) { !padShown() && ui.activity.hasWindowFocus() }
        ui.runOnUiThread { model.back(); model.guide() }
        ui.waitUntil(10_000) { ui.onAllNodesWithTag("guide-row:$sport").fetchSemanticsNodes().isNotEmpty() && shown("Spor programı") }
        // A cell of the opened channel past the programme on now opens its detail.
        val later = SemanticsMatcher("a cell of the channel after its programme") { node ->
            node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("guide-cell:$sport:") == true &&
                node.config.getOrNull(SemanticsProperties.ContentDescription)?.none { it.contains("Spor programı") } == true
        }
        // The guide shows one day: in the hour before midnight the programme ends past it, and no cell follows.
        assumeTrue(ui.onAllNodes(later).fetchSemanticsNodes().isNotEmpty())
        ui.onAllNodes(later)[0].performSemanticsAction(SemanticsActions.OnClick)
        ui.waitUntil(5_000) { ui.onAllNodesWithTag("guide-detail-reader").fetchSemanticsNodes().isNotEmpty() }
        // The app leaves the screen: the lock comes back, and the detail goes with the channel's programmes.
        ui.runOnUiThread { model.appStopped() }
        ui.waitUntil(5_000) { ui.onAllNodesWithTag("guide-detail-reader").fetchSemanticsNodes().isEmpty() }
        ui.onNode(hasText("Kilitli kanal") and hasAnyAncestor(hasTestTag("guide-row:$sport"))).assertExists()
        assertFalse(shown("Spor programı"))
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

    @Test fun sourceManagementAsksForThePinWhereverItOpensAndThePinHoldsInKaynaklar() {
        makePin()
        val source = model.state.value.sources.single().id
        // Ayarlar → Kaynakları yönet: the pad over the panel; Back opens nothing, and focus is back on the row.
        openSettings()
        row("Kaynakları yönet").performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        padTitle("PIN gir")
        ui.onNodeWithText("Kaynak ayarlarını açmak için PIN gir.").assertExists()
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { !padShown() && rowFocused("Kaynakları yönet") }
        assertEquals(Route.HOME, model.state.value.route)
        // The right PIN opens Kaynaklar, the panel closing behind it; it opens nothing else in Ayarlar.
        openPad()
        digits("2580")
        waitFor { model.state.value.route == Route.SOURCES }
        ui.waitUntil(5_000) { !padShown() && ui.onAllNodesWithTag("settings-panel").fetchSemanticsNodes().isEmpty() }
        assertTrue(parental.sourcesUnlocked); assertFalse(parental.adminUnlocked)
        // Within Kaynaklar the PIN holds: Düzenle opens the form without asking.
        ui.waitUntil(5_000) { focused("add-source") }
        ui.onNodeWithTag("edit-source:$source").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { model.state.value.route == Route.SETUP }
        assertFalse(padShown())
        ui.runOnUiThread { model.back() }
        assertEquals(Route.SOURCES, model.state.value.route)
        // Leaving Kaynaklar ends it: the guide's Kaynaklar asks again, and Back leaves the guide as it was.
        ui.runOnUiThread { model.back() }
        assertEquals(Route.HOME, model.state.value.route); assertFalse(parental.sourcesUnlocked)
        ui.runOnUiThread { model.guide() }
        ui.waitUntil(10_000) { ui.onAllNodesWithTag("guide-sources").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithTag("guide-sources").performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        ui.onNodeWithText("Kaynak ayarlarını açmak için PIN gir.").assertExists()
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { !padShown() && focused("guide-sources") }
        assertEquals(Route.GUIDE, model.state.value.route)
        ui.runOnUiThread { model.home() }
        // Kaynağı düzenle and Kaynağı kaldır in Ayarlar ask as well; Back keeps the source as it is.
        openSettings()
        row("Kaynağı düzenle").performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { !padShown() && rowFocused("Kaynağı düzenle") }
        assertEquals(Route.HOME, model.state.value.route)
        row("Kaynağı kaldır").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Kaynağı kaldır?")
        ui.onNodeWithTag("settings-confirm").performSemanticsAction(SemanticsActions.RequestFocus)
        openPad()
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { !padShown() && focused("settings-confirm") }
        assertEquals(listOf(source), model.state.value.sources.map { it.id }); assertEquals(Route.HOME, model.state.value.route)
        key(KeyEvent.KEYCODE_BACK)
        title("Ayarlar")
        key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)
        // "Kaynak yönetimi PIN istesin" off (with the PIN verified for Ebeveyn denetimi): Kaynaklar opens at once.
        openParentalPage()
        ui.onNodeWithTag("parental-sources-need-pin").assertIsOn().performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { !parental.sourcesNeedPin }
        ui.onNodeWithTag("parental-sources-need-pin").assertIsFocused().assertIsOff()
        assertFalse((ProtectionStore(context).read() as ProtectionRead.Ready).snapshot.options.sourcesNeedPin)
        key(KeyEvent.KEYCODE_BACK); key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)
        ui.runOnUiThread { assertFalse(model.manageSources()) }
        assertEquals(Route.SOURCES, model.state.value.route)
        ui.runOnUiThread { model.back() }
    }

    @Test fun bilgilerLocksTheWholeSourceAndItsLockGoesWithThePinThatOpenedKaynaklar() {
        makePin()
        val source = model.state.value.sources.single().id
        val (film, channel) = listOf("movie:2", "live:1").map { model.fixtureKey(it) }
        val rule = AccessRule(AccessScope.Source(source), pinRequired = true)
        fun openInfo() {
            ui.onNodeWithTag("info-source:$source").performSemanticsAction(SemanticsActions.RequestFocus)
            key(KeyEvent.KEYCODE_DPAD_CENTER)
            ui.waitUntil(5_000) { ui.onAllNodesWithTag("source-lock").fetchSemanticsNodes().isNotEmpty() }
            ui.onNodeWithTag("source-lock").performSemanticsAction(SemanticsActions.RequestFocus)
        }
        // Kaynağı kilitle: at once, without the PIN; every channel, film and series of the source asks for it then.
        openSourcesWithPin()
        openInfo()
        ui.onNodeWithTag("source-lock").assertTextEquals("Kaynağı kilitle")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { source in model.state.value.locks.sources }
        assertTrue(model.state.value.locks.ids.containsAll(listOf(film, channel)))
        assertTrue(rules().contains(rule))
        assertFalse(padShown())
        ui.onNodeWithTag("source-lock").assertTextEquals("Kaynağın kilidini kaldır")
        ui.onNode(hasText("Bu kaynaktaki bütün kanallar, filmler ve diziler PIN ister.", substring = true) and
            hasAnyAncestor(hasTestTag("source-info-reader"))).assertExists()
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { ui.onAllNodesWithTag("source-lock").fetchSemanticsNodes().isEmpty() }
        ui.onNodeWithTag("source-locked:$source", useUnmergedTree = true).assertExists()
        ui.runOnUiThread { model.back() }
        ui.runOnUiThread { assertTrue(model.open(entry(film))); assertTrue(model.open(entry(channel))) }
        assertEquals(Route.HOME, model.state.value.route)
        // Kaynağın kilidini kaldır with the PIN that opened Kaynaklar: nothing more is asked.
        openSourcesWithPin()
        openInfo()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { source !in model.state.value.locks.sources }
        assertFalse(padShown()); assertFalse(rules().contains(rule))
        assertTrue(model.state.value.locks.ids.isEmpty())
        key(KeyEvent.KEYCODE_BACK)
        ui.runOnUiThread { model.back() }

        // With "Kaynak yönetimi PIN istesin" off Kaynaklar opens at once, and removing the lock asks for the PIN over
        // Bilgiler, focus going back to it.
        lock(rule, options = ProtectionOptions(sourcesNeedPin = false))
        ui.runOnUiThread { assertFalse(model.manageSources()) }
        ui.waitUntil(5_000) { focused("add-source") }
        openInfo()
        ui.onNodeWithTag("source-lock").assertTextEquals("Kaynağın kilidini kaldır")
        openPad()
        ui.onNodeWithText("“Deneme kütüphanesi” kilidini kaldırmak için PIN gir.").assertExists()
        digits("2580")
        waitFor { source !in model.state.value.locks.sources }
        ui.waitUntil(5_000) { !padShown() && focused("source-lock") }
        ui.onNodeWithTag("source-lock").assertTextEquals("Kaynağı kilitle")
        assertFalse(parental.sourcesUnlocked); assertFalse(parental.adminUnlocked)
        key(KeyEvent.KEYCODE_BACK)
        ui.runOnUiThread { model.back() }
    }

    @Test fun theLaunchPinLocksTheAppOnReturnAndAtLaunchAndThePlayerWaitsUnderIt() {
        makePin()
        val channel = model.fixtureKey("live:1")
        // Ebeveyn denetimi → Açılışta PIN iste: on, without asking more.
        openParentalPage()
        ui.onNodeWithTag("parental-launch-needs-pin").assertIsOff().performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { parental.launchNeedsPin }
        ui.onNodeWithTag("parental-launch-needs-pin").assertIsOn()
        assertTrue((ProtectionStore(context).read() as ProtectionRead.Ready).snapshot.options.launchNeedsPin)
        key(KeyEvent.KEYCODE_BACK); key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)
        assertFalse(parental.appLocked)
        // A channel plays; Home, standby or another app: back on screen the lock screen covers the player.
        ui.runOnUiThread { model.showKind(MediaKind.LIVE); assertFalse(model.open(entry(channel))) }
        ui.waitUntil(5_000) { model.state.value.route == Route.PLAYER && playingId == channel }
        ui.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        assertTrue(parental.appLocked)
        assertEquals(Route.LOCKED, model.state.value.screen); assertEquals(Route.PLAYER, model.state.value.route)
        ui.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        ui.waitUntil(5_000) { lockShown() && focused("pin-key:1") }
        ui.onNodeWithTag("app-lock-title").assertTextEquals("NEXTV kilitli")
        ui.onNodeWithText("Devam etmek için PIN gir.").assertExists()
        assertTrue(ui.onAllNodesWithTag("player-panel").fetchSemanticsNodes().isEmpty())
        // Nothing opens behind it.
        ui.runOnUiThread { assertFalse(model.open(entry(channel))) }
        assertEquals(Route.PLAYER, model.state.value.route)
        // A wrong PIN keeps it; the right one brings the channel back, and opens nothing in Ayarlar.
        digits("1111")
        waitFor { parental.prompt?.feedback != null }
        feedback("PIN yanlış. Tekrar dene.")
        assertTrue(parental.appLocked)
        digits("2580")
        waitFor { !parental.appLocked }
        ui.waitUntil(5_000) { !lockShown() && ui.onAllNodesWithTag("player-panel").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(channel, playingId); assertFalse(parental.adminUnlocked)
        ui.runOnUiThread { model.back() }
        // Opening the app: the lock screen once the sources are open.
        ui.runOnUiThread { ui.activity.viewModelStore.clear() }
        ui.activityRule.scenario.recreate()
        ui.runOnUiThread { model = ViewModelProvider(ui.activity)[TvViewModel::class.java] }
        ui.waitUntil(20_000) { model.state.value.route == Route.HOME && model.state.value.sourceTask == SourceTask.IDLE }
        assertTrue(parental.appLocked)
        ui.waitUntil(5_000) { lockShown() && focused("pin-key:1") }
        assertTrue(ui.onAllNodesWithTag("home-content").fetchSemanticsNodes().isEmpty())
        digits("2580")
        waitFor { !parental.appLocked }
        ui.waitUntil(5_000) { !lockShown() && ui.onAllNodesWithTag("home-content").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun anUnreadableRecordLocksTheAppUntilItReadsAgainOrTheTvStartsOver() {
        makePin()
        val film = model.fixtureKey("movie:2")
        // The record's file gone while its key stays: it cannot be read, and is never taken for no PIN (D049).
        assertTrue(ProtectionStore(context).file.delete())
        ui.runOnUiThread { model.restore() }
        ui.waitUntil(20_000) { model.state.value.route == Route.HOME && model.state.value.sourceTask == SourceTask.IDLE }
        assertEquals(ParentalStatus.UNAVAILABLE, parental.status); assertTrue(parental.appLocked)
        ui.waitUntil(5_000) { lockShown() && focused("app-lock-retry") }
        ui.onNodeWithTag("app-lock-title").assertTextEquals("Ebeveyn denetimi kaydı açılamadı")
        assertTrue(ui.onAllNodesWithTag("pin-key:1").fetchSemanticsNodes().isEmpty())
        ui.runOnUiThread { assertFalse(model.open(entry(film))) }
        assertEquals(Route.HOME, model.state.value.route)
        // Yeniden dene while it still cannot be read: still locked.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        Thread.sleep(1_000); ui.waitForIdle()
        assertTrue(parental.appLocked); assertTrue(lockShown())
        // Sıfırla…: Vazgeç first and back on it; then the TV starts over.
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithTag("app-lock-reset").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { focused("pin-forgot-cancel") }
        padTitle("Bu cihaz sıfırlansın mı?")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { focused("app-lock-reset") }
        assertEquals(1, model.state.value.sources.size)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { focused("pin-forgot-cancel") }
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("pin-forgot-reset").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        waitFor { model.state.value.route == Route.SETUP && parental.status == ParentalStatus.OFF }
        assertFalse(parental.appLocked)
        ui.waitUntil(5_000) { !lockShown() }
        assertTrue(model.state.value.sources.isEmpty())
        assertEquals(ProtectionRead.Unconfigured, ProtectionStore(context).read())
    }
}
