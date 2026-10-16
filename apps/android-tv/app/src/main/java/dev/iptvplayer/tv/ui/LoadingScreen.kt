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
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LineIcon("tv", Ink.Mint, Modifier.size(20.dp))
                Text("IPTV PLAYER", color = Ink.Mint, fontFamily = Roboto, fontSize = 12.sp)
            }
            Text(when (state.sourceTask) {
                SourceTask.RESETTING -> "Kaynakların sıfırlanıyor"
                SourceTask.REMOVING -> "Kaynağın kaldırılıyor"
                SourceTask.SAVING -> "Kütüphanen kaydediliyor"
                else -> "Kütüphanen hazırlanıyor"
            }, Modifier.testTag("loading-title"), color = Ink.Text, fontFamily = Roboto, fontSize = 28.sp, lineHeight = 34.sp)
            Column(Modifier.fillMaxWidth().background(Ink.Background, RoundedCornerShape(12.dp)).padding(16.dp)
                .testTag("loading-status").semantics {
                    liveRegion = LiveRegionMode.Polite
                    progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(state.progressLabel, color = Ink.Text, fontFamily = Roboto, fontSize = 14.sp, lineHeight = 20.sp)
            }
            Note(when (state.sourceTask) {
                SourceTask.IMPORTING -> "Büyük listelerin yüklenmesi biraz zaman alabilir."
                SourceTask.RESETTING -> "İşlem tamamlanınca kaynaklarını yeniden ekleyebilirsin."
                SourceTask.REMOVING -> "Bu kaynağın cihazdaki listesi, favorileri ve izleme kayıtları temizleniyor."
                SourceTask.RESTORING -> "Bu cihazdaki kaynakların ve listelerin açılıyor."
                else -> "Kayıt tamamlanınca kütüphanen açılacak."
            }, modifier = Modifier.testTag("loading-explanation"))
            if (importing) Action("İptal", Modifier.focusRequester(cancel).testTag("loading-cancel"), onClick = cancelImport)
        }
    }
    LaunchedEffect(importing) { if (importing) cancel.requestFocus() }
}
