package dev.iptvplayer.tv

import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
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
 * unuttum. Each PIN check or save runs the real KDF, so the waits are long. Removes this app's protection record before
 * and after each test: a dedicated test emulator only, never a TV or an emulator holding a real source. Synthetic
 * sources and PINs.
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
                    "get_live_categories", "get_vod_categories", "get_series_categories" -> """[{"category_id":"1","category_name":"Genel"}]"""
                    "get_live_streams" -> """[{"stream_id":1,"name":"Deneme kanalı","category_id":"1"}]"""
                    "get_vod_streams" -> """[{"stream_id":2,"name":"Deneme filmi","category_id":"1","container_extension":"mp4"}]"""
                    "get_series" -> """[{"series_id":4,"name":"Deneme dizisi","category_id":"1"}]"""
                    else -> "[]"
                }
                return MockResponse().addHeader("Content-Type", "application/json").setBody(body)
            }
        }
        server.start()
        model = resetFixtureSources(ui)
        ui.runOnUiThread {
            model.draft(SourceDraft("Deneme kütüphanesi", SourceType.XTREAM, server.url("/").toString(), "synthetic", "synthetic"))
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
        ui.onNodeWithTag("pin-feedback").assertContentDescriptionEquals("Çok fazla yanlış deneme. 30 saniye sonra tekrar deneyebilirsin.")
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
