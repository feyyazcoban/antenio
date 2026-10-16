package dev.iptvplayer.tv

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import org.json.JSONObject
import org.junit.*
import org.junit.runner.RunWith
import java.io.File

/** Explicit development-only provisioning. Never packaged in the application APK. */
@RunWith(AndroidJUnit4::class)
class DeviceProvisioningTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    @Test fun installAuthorizedSourceWithoutKeyboardTranslation() {
        Assume.assumeTrue("Provisioning requires explicit opt-in",
            InstrumentationRegistry.getArguments().getString("provisionAuthorizedSource") == "true")
        val input = File(ui.activity.filesDir, "authorized-source.json")
        Assume.assumeTrue("Private provisioning input is required", input.isFile)
        val resultFile = File(ui.activity.filesDir, "provision-result.json")
        resultFile.delete()
        try {
            Assert.assertTrue("Existing sources will not be overwritten", DeviceStore(ui.activity).loadSources().sources.isEmpty())
            val json = JSONObject(input.readText())
            val model = ViewModelProvider(ui.activity)[TvViewModel::class.java]
            ui.waitUntil(10_000) { model.state.value.sourceTask == SourceTask.IDLE }
            ui.runOnUiThread {
                model.draft(SourceDraft("Evim", SourceType.XTREAM, json.getString("base"),
                    json.getString("username"), json.getString("password")))
                model.connectDraft()
            }
            ui.waitUntil(180_000) { model.state.value.route != Route.LOADING }
            Assert.assertTrue("Source could not be loaded", model.state.value.route == Route.HOME)
            val saved = DeviceStore(ui.activity).loadSources().sources.singleOrNull()
            Assert.assertNotNull("Source was not saved", saved)
            Assert.assertTrue("Credentials did not round-trip exactly",
                saved!!.username == json.getString("username") && saved.password == json.getString("password"))
            resultFile.writeText(JSONObject().apply {
                put("status", "configured")
                put("catalogCount", model.state.value.catalog.items.size)
                put("credentialsVerified", true)
                put("playbackStarted", false)
                for (kind in listOf(MediaKind.LIVE, MediaKind.MOVIE, MediaKind.SERIES)) {
                    put(kind.name, model.state.value.catalog.items.count { it.kind == kind && !it.adult })
                }
            }.toString(2))
        } finally {
            input.delete()
        }
    }
}
