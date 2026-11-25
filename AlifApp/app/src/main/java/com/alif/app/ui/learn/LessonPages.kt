package com.alif.app.ui.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alif.app.data.learn.Alphabet
import com.alif.app.data.learn.Example
import com.alif.app.data.learn.LessonPage
import com.alif.app.data.learn.LetterForm
import com.alif.app.data.learn.QuizItem
import com.alif.app.data.learn.SimilarGroup
import com.alif.app.ui.components.ArabicText
import com.alif.app.ui.components.SectionCard
import com.alif.app.ui.components.rememberHaptics

/** Кнопка «послушать». */
@Composable
fun PlayButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val haptics = rememberHaptics()
    FilledTonalIconButton(onClick = { haptics.tap(); onClick() }, modifier = modifier) {
        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Прослушать")
    }
}

@Composable
fun LessonPageView(
    page: LessonPage,
    vm: LessonViewModel,
    favorites: Set<String>,
) {
    when (page) {
        is LessonPage.Info -> InfoPage(page, vm::play)
        is LessonPage.LetterIntro -> LetterIntroPage(page.letterId, vm, favorites)
        is LessonPage.FormsTable -> FormsTablePage(page.letterIds, vm::playLetter)
        is LessonPage.SimilarLetters -> SimilarLettersPage(page.groups, vm::playLetter)
        LessonPage.JoinBuilder -> JoinBuilderPage()
        is LessonPage.Grid -> GridPage(page.title, page.body, page.items, vm::play)
    }
}

@Composable
private fun InfoPage(page: LessonPage.Info, onPlay: (Example) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(page.title, style = MaterialTheme.typography.headlineSmall)
        page.arabic?.let {
            SectionCard(Modifier.fillMaxWidth()) {
                ArabicText(it, scale = 1.6f, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)
            }
        }
        Text(page.body, style = MaterialTheme.typography.bodyLarge)
        page.examples.forEach { ex -> ExampleRow(ex, onPlay) }
    }
}

@Composable
fun ExampleRow(ex: Example, onPlay: (Example) -> Unit) {
    SectionCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (ex.audioKey != null || ex.surah > 0) PlayButton({ onPlay(ex) })
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                ArabicText(ex.arabic, quran = ex.surah > 0, scale = 1.1f, textAlign = TextAlign.End)
                Text(ex.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
private fun LetterIntroPage(letterId: String, vm: LessonViewModel, favorites: Set<String>) {
    val letter = Alphabet.get(letterId)
    var replay by remember(letterId) { mutableIntStateOf(0) }
    val key = "letter:${letter.id}"
    val colors = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Буква «${letter.name}»", style = MaterialTheme.typography.headlineSmall)
                Text("Звук: ${letter.sound}", style = MaterialTheme.typography.titleMedium, color = colors.secondary)
            }
            IconButton(onClick = { vm.toggleFavorite(key, letter.char, letter.name, letter.sound) }) {
                Icon(
                    if (key in favorites) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "В избранное",
                    tint = if (key in favorites) colors.secondary else colors.onSurfaceVariant,
                )
            }
        }

        // Крупная буква + анимация написания
        SectionCard(Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LetterWritingAnimation(
                    char = letter.char,
                    color = colors.primary,
                    guideColor = colors.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    replayKey = replay,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { replay++ }) {
                        Icon(Icons.Filled.Replay, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Как писать")
                    }
                    PlayButton({ vm.playLetter(letter.id) })
                }
            }
        }

        Text(letter.tip, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth())

        // Четыре формы
        SectionCard(Modifier.fillMaxWidth()) {
            Column {
                Text("Формы в слове", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    // справа налево: отдельная, начальная, срединная, конечная
                    LetterForm.entries.reversed().forEach { f ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ArabicText(letter.form(f), fontSize = 34.sp, color = colors.primary)
                            Text(f.title, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        }
                    }
                }
                if (!letter.joinsLeft) {
                    Text(
                        "Эта буква не соединяется со следующей (слева).",
                        style = MaterialTheme.typography.bodySmall, color = colors.secondary,
                    )
                }
            }
        }

        Alphabet.similarFor(letter.id)?.let { g -> SimilarGroupCard(g, vm::playLetter) }
    }
}

@Composable
private fun SimilarGroupCard(group: SimilarGroup, onPlay: (String) -> Unit) {
    SectionCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Не перепутайте", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                group.letterIds.reversed().forEach { id ->
                    val l = Alphabet.get(id)
                    Surface(onClick = { onPlay(id) }, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainer) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            ArabicText(l.char, fontSize = 40.sp, color = MaterialTheme.colorScheme.primary)
                            Text(l.name, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            Text(group.tip, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SimilarLettersPage(groups: List<SimilarGroup>, onPlay: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Похожие буквы", style = MaterialTheme.typography.headlineSmall)
        groups.forEach { SimilarGroupCard(it, onPlay) }
    }
}

/** Интерактивная таблица форм: нажмите на клетку — форма покажется крупно. */
@Composable
private fun FormsTablePage(letterIds: List<String>, onPlay: (String) -> Unit) {
    var selected by remember { mutableStateOf(letterIds.first() to LetterForm.ISOLATED) }
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Таблица форм", style = MaterialTheme.typography.headlineSmall)
        Text("Нажмите на любую клетку, чтобы увидеть форму крупно и услышать букву.", style = MaterialTheme.typography.bodyMedium)

        SectionCard(Modifier.fillMaxWidth()) {
            val l = Alphabet.get(selected.first)
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(selected, label = "form") { (id, f) ->
                    ArabicText(Alphabet.get(id).form(f), fontSize = 96.sp, color = colors.primary)
                }
                Text("${l.name} · ${selected.second.title.lowercase()} форма", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Заголовок: справа налево
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(1.1f))
            LetterForm.entries.reversed().forEach { f ->
                Text(f.title, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, color = colors.onSurfaceVariant)
            }
        }
        letterIds.forEach { id ->
            val l = Alphabet.get(id)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(l.name, Modifier.weight(1.1f), style = MaterialTheme.typography.titleSmall)
                LetterForm.entries.reversed().forEach { f ->
                    val isSel = selected == (id to f)
                    Surface(
                        onClick = { selected = id to f; onPlay(id) },
                        modifier = Modifier.weight(1f).padding(3.dp).aspectRatio(1f),
                        shape = MaterialTheme.shapes.small,
                        color = if (isSel) colors.secondaryContainer else colors.surfaceContainerLowest,
                        border = BorderStroke(1.dp, if (isSel) colors.secondary else colors.outlineVariant),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            ArabicText(l.form(f), fontSize = 26.sp)
                        }
                    }
                }
            }
        }
    }
}

/** Конструктор: выбирайте буквы — видно, как они соединяются в слово. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JoinBuilderPage() {
    val chosen = remember { mutableStateListOf<String>() }
    val haptics = rememberHaptics()
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Конструктор слов", style = MaterialTheme.typography.headlineSmall)
        SectionCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().heightIn(min = 150.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Слитно:", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                ArabicText(if (chosen.isEmpty()) "…" else chosen.joinToString(""), fontSize = 64.sp, color = colors.primary)
                Text("По буквам:", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                ArabicText(chosen.joinToString("  "), fontSize = 28.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { if (chosen.isNotEmpty()) chosen.removeAt(chosen.lastIndex) }) {
                Icon(Icons.AutoMirrored.Filled.Backspace, null)
                Spacer(Modifier.width(6.dp)); Text("Стереть")
            }
            TextButton(onClick = { chosen.clear() }) { Text("Очистить") }
        }
        val nonJoining = chosen.dropLast(1).filter { c -> Alphabet.byChar(c)?.joinsLeft == false }
        if (nonJoining.isNotEmpty()) {
            Text(
                "Разрыв после: ${nonJoining.distinct().joinToString(" ")} — эти буквы не соединяются слева.",
                style = MaterialTheme.typography.bodyMedium, color = colors.secondary,
            )
        }
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Alphabet.letters.forEach { l ->
                Surface(
                    onClick = { haptics.tap(); if (chosen.size < 8) chosen.add(l.char) },
                    modifier = Modifier.size(48.dp),
                    shape = MaterialTheme.shapes.small,
                    color = colors.surfaceContainerLowest,
                    border = BorderStroke(1.dp, colors.outlineVariant),
                ) { Box(contentAlignment = Alignment.Center) { ArabicText(l.char, fontSize = 24.sp) } }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GridPage(title: String, body: String, items: List<QuizItem>, onPlay: (QuizItem) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(body, style = MaterialTheme.typography.bodyMedium)
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { item ->
                Surface(
                    onClick = { onPlay(item) },
                    modifier = Modifier.widthIn(min = 72.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = colors.surfaceContainerLowest,
                    border = BorderStroke(1.dp, colors.outlineVariant),
                ) {
                    Column(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        ArabicText(item.arabic, fontSize = 30.sp, color = colors.primary)
                        Text(item.answer, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/** Этап «Послушай и повтори». */
@Composable
fun ListenPhase(items: List<QuizItem>, vm: LessonViewModel) {
    val anyAudio = items.any { vm.hasAudio(it) || it.surah > 0 }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Послушай и повтори", style = MaterialTheme.typography.headlineSmall)
        Text(
            if (anyAudio) "Нажмите на каждую строку, послушайте и повторите вслух 2–3 раза."
            else "Записи для этого урока ещё не добавлены (см. список аудиофайлов). Пока прочитайте вслух по транскрипции.",
            style = MaterialTheme.typography.bodyMedium,
        )
        items.forEach { item ->
            Surface(
                onClick = { vm.play(item) },
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.VolumeUp, null) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.answer, style = MaterialTheme.typography.titleMedium)
                        item.meaning?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    ArabicText(item.arabic, quran = item.surah > 0, scale = 1.1f)
                }
            }
        }
    }
}
