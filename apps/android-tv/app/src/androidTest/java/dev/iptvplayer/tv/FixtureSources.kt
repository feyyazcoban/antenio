package dev.iptvplayer.tv

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue

/** Clears all synthetic accounts, including after an interrupted multi-source test. Never use on a TV. */
internal fun resetFixtureSources(ui: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>): TvViewModel {
    assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
    ui.runOnUiThread { ui.activity.viewModelStore.clear() }
    runBlocking { SourceTransactions.shared.write {
        DeviceStore(ui.activity).apply {
            forgetSource()
            // A Sırala choice tried by hand on this emulator would reorder the fixtures' sections.
            librarySort(MediaKind.MOVIE, LibrarySort.DEFAULT); librarySort(MediaKind.SERIES, LibrarySort.DEFAULT)
            // Oynatıcı settings stay with the TV as well; "Hiçbir zaman" left from a run would keep every player's controls up.
            playback = PlaybackPreferences()
        }
        CatalogStore(ui.activity).clear(); EpgStore(ui.activity).clear()
    } }
    ui.activityRule.scenario.recreate(); ui.waitForIdle()
    lateinit var model: TvViewModel
    ui.runOnUiThread { model = ViewModelProvider(ui.activity)[TvViewModel::class.java] }
    ui.waitUntil(10_000) { model.state.value.route == Route.SETUP && model.state.value.sourceTask == SourceTask.IDLE }
    return model
}

internal fun TvViewModel.fixtureKey(local: String) = ContentRef(state.value.sources.single().id, local).key

internal fun Catalog.fixtureRaw() = Catalog(items.map { it.withoutSource() },
    m3uEpisodes.mapKeys { ContentRef.parse(it.key)?.localId ?: it.key }.mapValues { (_, value) -> value.map { it.withoutSource() } }, warnings, epgUrls, categoryOrder)
