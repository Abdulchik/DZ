package com.alif.app.ui.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.alif.app.ui.theme.ArabicTextStyle
import com.alif.app.ui.theme.LocalAppSettings
import com.alif.app.ui.theme.QuranTextStyle

/**
 * Арабский текст с правильным направлением справа налево.
 *
 * @param scale множитель к размеру из настроек (например, 3f для крупной буквы в уроке)
 * @param quran true — шрифт Корана (Amiri Quran), false — шрифт уроков (Amiri)
 */
@Composable
fun ArabicText(
    text: String,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    quran: Boolean = false,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign = TextAlign.Start,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
) {
    val baseSize = LocalAppSettings.current.arabicFontSize
    val size = if (fontSize != TextUnit.Unspecified) fontSize else (baseSize * scale).sp
    val style = (if (quran) QuranTextStyle else ArabicTextStyle).copy(
        fontSize = size,
        // Арабским огласовкам нужен запас по высоте строки, иначе знаки обрезаются
        lineHeight = if (quran) 2.0.em else 1.7.em,
        textDirection = TextDirection.Rtl,
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = text,
            modifier = modifier,
            style = style,
            color = color,
            textAlign = textAlign,
            maxLines = maxLines,
        )
    }
}
