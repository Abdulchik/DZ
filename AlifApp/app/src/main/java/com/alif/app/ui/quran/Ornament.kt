package com.alif.app.ui.quran

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Номер аята в восьмиконечном орнаментальном знаке. */
@Composable
fun AyahNumberBadge(number: Int, modifier: Modifier = Modifier) {
    val gold = MaterialTheme.colorScheme.secondary
    val fill = MaterialTheme.colorScheme.secondaryContainer
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val side = size.minDimension * 0.7f
            val tl = androidx.compose.ui.geometry.Offset((size.width - side) / 2, (size.height - side) / 2)
            val sz = androidx.compose.ui.geometry.Size(side, side)
            for (angle in listOf(0f, 45f)) {
                rotate(angle) {
                    drawRect(fill, tl, sz)
                    drawRect(gold, tl, sz, style = Stroke(width = 1.5.dp.toPx()))
                }
            }
            drawCircle(gold, radius = side * 0.42f, style = Stroke(width = 1.dp.toPx()))
        }
        Text(
            number.toString(),
            fontSize = if (number > 99) 11.sp else 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

/** Восточно-арабские цифры: 12 → ١٢ */
fun arabicDigits(n: Int): String = n.toString().map { '٠' + (it - '0') }.joinToString("")
