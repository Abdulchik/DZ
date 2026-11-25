package com.alif.app.ui.learn

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.alif.app.data.learn.Alphabet
import com.alif.app.data.learn.Exercise
import com.alif.app.data.learn.ExerciseKind
import com.alif.app.data.learn.LetterForm
import com.alif.app.data.learn.QuizItem
import com.alif.app.ui.components.ArabicText
import com.alif.app.ui.components.rememberHaptics
import com.alif.app.ui.theme.ArabicTextStyle
import com.alif.app.ui.theme.Correct
import com.alif.app.ui.theme.Wrong

@Composable
fun ExerciseView(ex: Exercise, state: LessonUiState, vm: LessonViewModel) {
    when (ex) {
        is Exercise.Choice -> ChoiceView(ex, state.answer, vm)
        is Exercise.FindForm -> FindFormView(ex, state.answer, vm)
        is Exercise.Build -> BuildView(ex, state.answer, vm)
        is Exercise.Read -> ReadView(ex, state, vm)
    }
}

private fun questionFor(item: QuizItem, kind: ExerciseKind): String {
    val type = item.key.substringBefore(':')
    return when (kind) {
        ExerciseKind.HEAR_CHOOSE -> "Послушайте и выберите, что прозвучало"
        ExerciseKind.CHOOSE_ARABIC -> when (type) {
            "letter" -> "Найдите букву «${item.answer}»"
            "tajweed", "sunmoon" -> "Где пример: «${item.answer}»?"
            else -> "Найдите, где написано «${item.answer}»"
        }
        else -> when (type) {
            "letter" -> "Как называется эта буква?"
            "tajweed" -> "Какое правило таджвида здесь?"
            "sunmoon" -> "Солнечная или лунная буква?"
            else -> "Как это читается?"
        }
    }
}

@Composable
private fun optionColors(isCorrectOption: Boolean, isChosen: Boolean, answered: Boolean): Pair<Color, Color> {
    val c = MaterialTheme.colorScheme
    val bg = when {
        !answered -> c.surfaceContainerLowest
        isCorrectOption -> Correct.copy(alpha = 0.18f)
        isChosen -> Wrong.copy(alpha = 0.18f)
        else -> c.surfaceContainerLowest
    }
    val border = when {
        !answered -> c.outlineVariant
        isCorrectOption -> Correct
        isChosen -> Wrong
        else -> c.outlineVariant
    }
    val animBg by animateColorAsState(bg, label = "bg")
    val animBorder by animateColorAsState(border, label = "border")
    return animBg to animBorder
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceView(ex: Exercise.Choice, answer: Answer?, vm: LessonViewModel) {
    val haptics = rememberHaptics()
    val item = ex.item
    if (ex.kind == ExerciseKind.HEAR_CHOOSE) {
        LaunchedEffect(item.key) { vm.play(item) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(questionFor(item, ex.kind), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        when (ex.kind) {
            ExerciseKind.HEAR_CHOOSE -> FilledIconButton(
                onClick = { vm.play(item) },
                modifier = Modifier.size(96.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) { Icon(Icons.AutoMirrored.Filled.VolumeUp, "Прослушать", Modifier.size(44.dp)) }

            ExerciseKind.SEE_CHOOSE -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ArabicText(item.arabic, quran = item.surah > 0, scale = if (item.arabic.length <= 3) 3f else 1.6f, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                if (vm.hasAudio(item) || item.surah > 0) PlayButton({ vm.play(item) })
            }
            else -> Unit
        }

        if (ex.kind == ExerciseKind.SEE_CHOOSE) {
            // Ответы — текстом, крупными кнопками
            ex.options.forEach { o ->
                val (bg, border) = optionColors(o.key == item.key, answer?.chosen == o.key, answer != null)
                Surface(
                    onClick = { if (answer == null) { haptics.tap(); vm.answerChoice(o) } },
                    shape = MaterialTheme.shapes.large,
                    color = bg,
                    border = BorderStroke(2.dp, border),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                        Text(o.answer, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            // Ответы — арабскими плитками 2×2
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 2,
            ) {
                ex.options.forEach { o ->
                    val (bg, border) = optionColors(o.key == item.key, answer?.chosen == o.key, answer != null)
                    Surface(
                        onClick = { if (answer == null) { haptics.tap(); vm.answerChoice(o) } },
                        shape = MaterialTheme.shapes.large,
                        color = bg,
                        border = BorderStroke(2.dp, border),
                        modifier = Modifier.weight(1f).heightIn(min = 110.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                            ArabicText(o.arabic, quran = o.surah > 0, scale = if (o.arabic.length <= 3) 1.8f else 1.0f, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

/** Слово, в котором одна буква выделена золотым цветом. */
@Composable
private fun HighlightedWord(word: String, index: Int) {
    val gold = MaterialTheme.colorScheme.secondary
    val text = buildAnnotatedString {
        word.forEachIndexed { i, ch ->
            if (i == index) withStyle(SpanStyle(color = gold)) { append(ch) } else append(ch)
        }
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text,
            style = ArabicTextStyle.copy(fontSize = 84.sp, textDirection = TextDirection.Rtl),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun FindFormView(ex: Exercise.FindForm, answer: Answer?, vm: LessonViewModel) {
    val haptics = rememberHaptics()
    val letter = Alphabet.get(ex.item.key.substringAfter(':'))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Какой формой стоит буква «${letter.name}» ${letter.char}?", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        HighlightedWord(ex.word, ex.index)
        Text("Выделена золотым цветом", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LetterForm.entries.forEach { f ->
            val (bg, border) = optionColors(f == ex.answer, answer?.chosen == f.name, answer != null)
            Surface(
                onClick = { if (answer == null) { haptics.tap(); vm.answerForm(f) } },
                shape = MaterialTheme.shapes.large,
                color = bg,
                border = BorderStroke(2.dp, border),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(f.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    ArabicText(letter.form(f), fontSize = 28.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BuildView(ex: Exercise.Build, answer: Answer?, vm: LessonViewModel) {
    val haptics = rememberHaptics()
    // Индексы выбранных плиток (одна буква может встречаться дважды)
    val picked = remember(ex) { mutableStateListOf<Int>() }
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Соберите слово", style = MaterialTheme.typography.titleLarge)
        Text("«${ex.item.answer}»" + (ex.item.meaning?.let { " — $it" } ?: ""), style = MaterialTheme.typography.titleMedium, color = colors.secondary, textAlign = TextAlign.Center)
        Text("Нажимайте буквы по порядку — справа налево", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)

        Surface(
            shape = MaterialTheme.shapes.large,
            color = colors.surfaceContainerLowest,
            border = BorderStroke(2.dp, when (answer?.correct) { true -> Correct; false -> Wrong; null -> colors.outlineVariant }),
            modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                ArabicText(picked.joinToString("") { ex.tiles[it] }.ifEmpty { "…" }, fontSize = 56.sp, color = colors.primary)
            }
        }
        if (answer?.correct == false) {
            Text("Правильно:", style = MaterialTheme.typography.labelMedium)
            ArabicText(ex.target.joinToString(""), fontSize = 40.sp, color = Correct)
        }

        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ex.tiles.forEachIndexed { i, ch ->
                val used = i in picked
                Surface(
                    onClick = { if (!used && answer == null) { haptics.tap(); picked.add(i) } },
                    modifier = Modifier.size(64.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = if (used) colors.surfaceVariant else colors.surfaceContainerLowest,
                    border = BorderStroke(1.dp, colors.outlineVariant),
                    shadowElevation = if (used) 0.dp else 2.dp,
                ) { Box(contentAlignment = Alignment.Center) { ArabicText(if (used) "" else ch, fontSize = 30.sp) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { if (picked.isNotEmpty() && answer == null) picked.removeAt(picked.lastIndex) }) {
                Icon(Icons.AutoMirrored.Filled.Backspace, null)
                Spacer(Modifier.width(6.dp)); Text("Стереть")
            }
            Button(
                onClick = { vm.answerBuild(picked.map { ex.tiles[it] }) },
                enabled = answer == null && picked.size == ex.target.size,
            ) { Text("Проверить") }
        }
    }
}

@Composable
private fun ReadView(ex: Exercise.Read, state: LessonUiState, vm: LessonViewModel) {
    val item = ex.item
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Прочитайте вслух", style = MaterialTheme.typography.titleLarge)
        ArabicText(item.arabic, quran = item.surah > 0, scale = if (item.arabic.length < 12) 2.2f else 1.3f, color = colors.primary, textAlign = TextAlign.Center)
        if (!state.revealed) {
            Text("Прочитайте сами, затем сверьтесь с ответом и аудио.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Button(onClick = vm::reveal, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Проверить себя") }
        } else {
            Surface(shape = MaterialTheme.shapes.large, color = colors.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(item.answer, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                    item.meaning?.let { Text(it, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center) }
                    Spacer(Modifier.height(8.dp))
                    PlayButton({ vm.play(item) })
                }
            }
            if (state.answer == null) {
                Text("Вы прочитали правильно?", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { vm.selfAssess(false) }, modifier = Modifier.weight(1f).height(56.dp)) { Text("Ошибся") }
                    Button(onClick = { vm.selfAssess(true) }, modifier = Modifier.weight(1f).height(56.dp)) { Text("Верно") }
                }
            }
        }
    }
}

/** Панель внизу после ответа: верно / неверно + правильный ответ. */
@Composable
fun FeedbackPanel(ex: Exercise, answer: Answer, onNext: () -> Unit) {
    val color = if (answer.correct) Correct else Wrong
    Surface(color = color.copy(alpha = 0.14f), shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = color, modifier = Modifier.size(12.dp)) {}
                Spacer(Modifier.width(10.dp))
                Text(
                    if (answer.correct) listOf("Отлично!", "Верно!", "Молодец!", "Так держать!").random() else "Не совсем",
                    style = MaterialTheme.typography.titleLarge, color = color,
                )
            }
            if (!answer.correct) {
                val correctText = when (ex) {
                    is Exercise.Choice -> "${ex.item.answer}  ·  ${ex.item.arabic}"
                    is Exercise.FindForm -> ex.answer.title + " форма"
                    is Exercise.Build -> ex.target.joinToString("")
                    is Exercise.Read -> ex.item.answer
                }
                Text("Правильный ответ: $correctText", style = MaterialTheme.typography.bodyLarge)
            }
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("Далее") }
        }
    }
}
