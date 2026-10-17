package dev.iptvplayer.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.SourceTask
import dev.iptvplayer.tv.TvState

@Composable internal fun LoadingScreen(state: TvState, cancelImport: () -> Unit) {
    val cancel = remember { FocusRequester() }
    val importing = state.sourceTask == SourceTask.IMPORTING
    Box(Modifier.fillMaxSize().background(Ink.Background).padding(horizontal = TvLayout.Gutter, vertical = 28.dp),
        contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()
            .background(Ink.Container, RoundedCornerShape(24.dp)).padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            NextvLogo(height = 24.dp)
            Text(stringResource(when (state.sourceTask) {
                SourceTask.RESETTING -> R.string.loading_resetting
                SourceTask.REMOVING -> R.string.loading_removing
                SourceTask.SAVING -> R.string.loading_saving
                else -> R.string.library_preparing
            }), Modifier.testTag("loading-title"), color = Ink.Text, fontFamily = Roboto, fontSize = 28.sp, lineHeight = 34.sp)
            Column(Modifier.fillMaxWidth().background(Ink.Background, RoundedCornerShape(12.dp)).padding(16.dp)
                .testTag("loading-status").semantics {
                    liveRegion = LiveRegionMode.Polite
                    progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LoadingArc()
                    Text(state.progressLabel.resolve(), color = Ink.Text, fontFamily = Roboto, fontSize = 15.sp, lineHeight = 21.sp)
                }
            }
            Note(stringResource(when (state.sourceTask) {
                SourceTask.IMPORTING -> R.string.loading_importing_note
                SourceTask.RESETTING -> R.string.loading_resetting_note
                SourceTask.REMOVING -> R.string.loading_removing_note
                SourceTask.RESTORING -> R.string.loading_restoring_note
                else -> R.string.loading_saving_note
            }), modifier = Modifier.testTag("loading-explanation"))
            if (importing) Action(stringResource(R.string.loading_cancel), Modifier.focusRequester(cancel).testTag("loading-cancel"), onClick = cancelImport)
        }
    }
    LaunchedEffect(importing) { if (importing) cancel.requestFocus() }
}
