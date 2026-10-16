package dev.iptvplayer.tv.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import dev.iptvplayer.tv.TvState
import dev.iptvplayer.tv.TvViewModel

@Composable internal fun SourceRecoveryScreen(state: TvState, model: TvViewModel) {
    val retry = remember { FocusRequester() }
    val reset = remember { FocusRequester() }
    var confirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(horizontal = TvLayout.Gutter, vertical = 28.dp)
        .testTag("source-recovery-screen"), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("IPTV PLAYER", color = Ink.Mint, fontSize = 14.sp, fontFamily = Roboto)
        Heading("Kütüphanen açılamadı")
        Column(Modifier.weight(1f, fill = false).fillMaxWidth().background(Ink.Container, RoundedCornerShape(24.dp))
            .padding(24.dp).verticalScroll(rememberScrollState()).padding(5.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Note(state.error ?: "Kayıtlı kaynaklar okunamadı. Yeniden deneyebilirsin.")
            Action("Yeniden dene", Modifier.focusRequester(retry).testTag("retry-source-recovery"), primary = true) { model.restore() }
            if (state.recoveryCanReset) {
                Note("Sorun devam ederse bu cihazdaki kaynakları sıfırlayıp hesaplarını yeniden ekleyebilirsin. Bunun için bağlantı veya hesap bilgilerin gerekecek.")
                Action("Kaynakları sıfırla…", Modifier.focusRequester(reset).testTag("open-source-reset")) { confirm = true }
            }
        }
    }
    LaunchedEffect(Unit) { retry.requestFocus() }
    if (confirm) {
        val cancel = remember { FocusRequester() }
        val firstDetail = remember { FocusRequester() }
        fun dismiss() { confirm = false; reset.requestFocus() }
        SourceDialog("Kaynakları sıfırla?", cancel, ::dismiss) {
            Column(Modifier.weight(1f, fill = false).testTag("reset-details-viewport")
                .verticalScroll(rememberScrollState()).padding(vertical = 5.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val paragraphs = listOf(
                    "Bu cihazdaki tüm kaynak hesapları ve listeler silinecek.",
                    "Afiş önbelleği, favoriler ve izleme geçmişi de silinecek. İşlem geri alınamaz.",
                    "Kaynaklarını yeniden eklemek için bağlantı veya hesap bilgilerin gerekir.",
                    "Sağlayıcı hesabın ve aboneliğin etkilenmez. Ses, hareket ve dil tercihlerin korunur.")
                paragraphs.forEachIndexed { index, paragraph ->
                    var focused by remember { mutableStateOf(false) }
                    Note(paragraph, modifier = Modifier.fillMaxWidth().testTag("reset-detail:$index")
                        .then(if (index == 0) Modifier.focusRequester(firstDetail) else Modifier)
                        .onFocusChanged { focused = it.isFocused }.focusable()
                        .border(TvLayout.Focus, if (focused) Ink.Mint else Color.Transparent, RoundedCornerShape(6.dp)).padding(8.dp))
                }
            }
            Column(Modifier.padding(vertical = 5.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Action("Vazgeç", Modifier.focusRequester(cancel).focusProperties { up = firstDetail }.testTag("cancel-source-reset")) { dismiss() }
                Action("Tüm kaynakları sıfırla", Modifier.testTag("confirm-source-reset")) { confirm = false; model.resetUnreadableSources() }
            }
        }
    }
}
