package dev.iptvplayer.tv.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import io.nayuki.qrcodegen.QrCode as Qr

/**
 * [text] as a QR code: dark modules on white with the standard four-module quiet zone, so phone cameras read it on a
 * dark TV screen. [description] is what TalkBack reads instead of the pattern.
 */
@Composable internal fun QrCode(text: String, size: Dp, description: String, modifier: Modifier = Modifier) {
    val qr = remember(text) { Qr.encodeText(text, Qr.Ecc.MEDIUM) }
    Canvas(modifier.size(size).background(Color.White, RoundedCornerShape(TvLayout.CardRadius))
        .semantics { contentDescription = description }) {
        val quiet = 4
        val modules = qr.size + 2 * quiet
        val cell = this.size.minDimension / modules
        for (y in 0 until qr.size) for (x in 0 until qr.size) if (qr.getModule(x, y))
            drawRect(Ink.Background, Offset((x + quiet) * cell, (y + quiet) * cell), Size(cell + .5f, cell + .5f))
    }
}
