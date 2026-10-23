package dev.iptvplayer.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.BillingState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The membership screen (D121): İzle after the free week, without the subscription. What the viewer must know before
 * buying is in view without scrolling: that Antenio sells no channels, the yearly price as Play writes it, that it renews
 * every year, how to cancel and where the terms are, as a QR code for the phone. Abone ol opens Play's own purchase
 * window; Kapat leaves everything else as it was. Without a plan from Play, Yeniden dene asks again.
 */
@Composable internal fun MembershipDialog(billing: BillingState, subscribe: () -> Unit, retry: () -> Unit, close: () -> Unit) {
    val first = remember { FocusRequester() }
    val offer = billing.offer
    Dialog(onDismissRequest = close, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Row(Modifier.width(900.dp).background(Ink.Container, RoundedCornerShape(18.dp)).padding(32.dp).testTag("membership"),
            horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                BrandLogo(height = 22.dp)
                Heading(stringResource(R.string.paywall_title))
                Note(stringResource(R.string.paywall_body))
                Box(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
                    if (offer != null) Text(stringResource(R.string.paywall_price, offer.price), Modifier.testTag("membership-price"),
                        color = Ink.Text, fontFamily = Roboto, fontSize = 18.sp, lineHeight = 24.sp)
                    else Note(stringResource(R.string.paywall_no_offer), error = true)
                }
                Note(stringResource(R.string.paywall_cancel))
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (offer != null) Action(stringResource(R.string.paywall_subscribe), Modifier.focusRequester(first)
                        .testTag("membership-subscribe"), primary = true, onClick = subscribe)
                    else Action(stringResource(R.string.action_retry), Modifier.focusRequester(first)
                        .testTag("membership-retry"), primary = true, onClick = retry)
                    Action(stringResource(R.string.action_close), Modifier.testTag("membership-close"), outlined = true, onClick = close)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val terms = stringResource(R.string.paywall_terms_url)
                QrCode(terms, 150.dp, stringResource(R.string.paywall_terms_qr))
                Note(stringResource(R.string.paywall_terms))
                Text(terms.removePrefix("https://"), color = Ink.Text, fontFamily = Roboto, fontSize = 14.sp, lineHeight = 19.sp)
            }
        }
        val window = LocalWindowInfo.current
        LaunchedEffect(offer != null) {
            withTimeoutOrNull(500) { snapshotFlow { window.isWindowFocused }.first { it } }
            withFrameNanos { }
            runCatching { first.requestFocus() }
        }
    }
}
