package dev.iptvplayer.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text

@Composable internal fun EpisodeEndDialog(s: EpisodeEndState, play: () -> Unit, stay: () -> Unit,
    returnToEpisodes: () -> Unit, replay: () -> Unit) {
    val first = remember { FocusRequester() }
    Dialog(onDismissRequest = stay, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().padding(horizontal = 38.dp, vertical = 26.dp), contentAlignment = Alignment.BottomEnd) {
            Column(Modifier.width(480.dp).background(Ink.Container, RoundedCornerShape(16.dp))
                .border(1.dp, Ink.Divider, RoundedCornerShape(16.dp)).padding(24.dp).testTag("episode-end")
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key in listOf(Key.MediaPause, Key.MediaPlayPause)) {
                        stay(); true
                    } else false
                }, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(if (s.next == null) "BÖLÜM TAMAMLANDI" else "SONRAKİ BÖLÜM", color = Ink.Mint,
                    fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 1.2.sp)
                val entry = s.next?.entry ?: s.current
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Artwork(entry.sourceId, entry.artwork, "", Modifier.width(112.dp).aspectRatio(16f / 9).clip(RoundedCornerShape(8.dp)))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(entry.title, color = Ink.Text, fontFamily = Roboto, fontSize = 20.sp, lineHeight = 26.sp,
                            fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(if (entry.episodeOrderKnown) "${entry.season}. sezon · ${entry.episode}. bölüm" else "Bölüm numarası belirtilmemiş", color = Ink.Muted,
                            fontFamily = Roboto, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (s.seconds != null) {
                        Text("${s.seconds} saniye sonra başlayacak", color = Ink.Text, fontFamily = Roboto, fontSize = 14.sp,
                            modifier = Modifier.testTag("episode-countdown"))
                        Box(Modifier.fillMaxWidth().height(3.dp).background(Ink.High)) {
                            Box(Modifier.fillMaxWidth(s.seconds.toFloat() / EpisodeEndController.COUNTDOWN_SECONDS)
                                .fillMaxHeight().background(Ink.Mint))
                        }
                    }
                    val note = s.notice ?: s.next?.notice ?: if (s.next == null)
                        "Bu kaynakta açılabilecek başka bölüm bulunamadı. Bölüm listesine dönebilir veya yeniden izleyebilirsin."
                        else if (s.seconds == null) "Hazır olduğunda sonraki bölümü başlatabilirsin." else null
                    if (note != null) Note(note)
                }
                // Cancel has the initial focus: a single OK prevents an unwanted automatic transition.
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Action("Bu bölümde kal", Modifier.focusRequester(first), primary = s.seconds != null, onClick = stay)
                    if (s.next != null) Action("Şimdi oynat", primary = s.seconds == null, icon = "play", onClick = play)
                    else Action("Baştan izle", icon = "play", onClick = replay)
                }
                Action("Bölümlere dön", onClick = returnToEpisodes)
                Text("Geri: bu bölümde kal", color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp)
            }
        }
    }
    LaunchedEffect(Unit) { withFrameNanos { }; first.requestFocus() }
}
