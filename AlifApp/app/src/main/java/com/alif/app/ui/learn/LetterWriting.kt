package com.alif.app.ui.learn

import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import com.alif.app.R

private class Contour(val path: Path, val length: Float, val bounds: RectF) {
    val area get() = bounds.width() * bounds.height()
}

/**
 * Анимация написания буквы: контур прорисовывается справа налево (так пишут по-арабски),
 * сначала основа буквы, затем точки и мелкие знаки. В конце буква заливается цветом.
 *
 * @param replayKey смените значение, чтобы проиграть анимацию заново
 */
@Composable
fun LetterWritingAnimation(
    char: String,
    color: Color,
    guideColor: Color,
    modifier: Modifier = Modifier,
    replayKey: Int = 0,
) {
    val context = LocalContext.current
    val typeface = remember { runCatching { ResourcesCompat.getFont(context, R.font.amiri_regular) }.getOrNull() }
    val progress = remember(char) { Animatable(0f) }
    LaunchedEffect(char, replayKey) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 2800, easing = LinearEasing))
    }

    Canvas(modifier) {
        val textSize = size.height * 0.62f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.textSize = textSize
        }
        val glyph = Path()
        paint.getTextPath(char, 0, char.length, 0f, 0f, glyph)
        val b = RectF()
        glyph.computeBounds(b, true)
        if (b.isEmpty) return@Canvas
        val dx = size.width / 2f - b.centerX()
        val dy = size.height / 2f - b.centerY()
        glyph.offset(dx, dy)

        // Разбиваем букву на отдельные контуры
        val contours = ArrayList<Contour>()
        val measure = PathMeasure(glyph, false)
        do {
            val len = measure.length
            if (len > 0f) {
                val seg = Path()
                measure.getSegment(0f, len, seg, true)
                val r = RectF()
                seg.computeBounds(r, true)
                contours.add(Contour(seg, len, r))
            }
        } while (measure.nextContour())
        if (contours.isEmpty()) return@Canvas

        // Крупные части — первыми, справа налево; точки (мелкие части) — в конце
        val maxArea = contours.maxOf { it.area }
        val (big, small) = contours.partition { it.area > maxArea * 0.15f }
        val ordered = big.sortedByDescending { it.bounds.right } + small.sortedByDescending { it.bounds.right }
        val total = ordered.sumOf { it.length.toDouble() }.toFloat()
        var remaining = progress.value * total

        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = textSize * 0.025f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            this.color = color.toArgb()
        }
        val guide = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = guideColor.toArgb()
        }
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = color.copy(alpha = ((progress.value - 0.85f) / 0.15f).coerceIn(0f, 1f)).toArgb()
        }

        drawIntoCanvas { canvas ->
            val c = canvas.nativeCanvas
            c.drawPath(glyph, guide) // бледный силуэт-подсказка
            for (ct in ordered) {
                if (remaining <= 0f) break
                val part = minOf(remaining, ct.length)
                val pm = PathMeasure(ct.path, false)
                val dst = Path()
                pm.getSegment(0f, part, dst, true)
                c.drawPath(dst, stroke)
                remaining -= ct.length
            }
            if (progress.value > 0.85f) c.drawPath(glyph, fill)
        }
    }
}
