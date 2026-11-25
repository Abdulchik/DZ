package com.alif.app.ui.learn

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.app.R
import com.alif.app.data.learn.Curriculum
import com.alif.app.data.learn.LearningModule
import com.alif.app.data.learn.Lesson
import com.alif.app.ui.LocalSnackbar
import com.alif.app.ui.appViewModelFactory
import com.alif.app.ui.components.ArabicText
import com.alif.app.ui.components.ScreenHeader
import com.alif.app.ui.components.SectionCard
import com.alif.app.ui.components.rememberHaptics
import kotlinx.coroutines.launch

/** Сдвиг кружков влево-вправо — «змейка». */
private val zigzag = listOf(0.dp, 48.dp, 72.dp, 48.dp, 0.dp, (-48).dp, (-72).dp, (-48).dp)

private sealed interface PathRow {
    data class Header(val module: LearningModule) : PathRow
    data class Node(val lesson: Lesson, val indexInPath: Int) : PathRow
}

@Composable
fun LearnScreen(
    onOpenLesson: (String) -> Unit,
    onOpenReview: () -> Unit,
    vm: LearnViewModel = viewModel(factory = appViewModelFactory { LearnViewModel(it) }),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val lockedMsg = stringResource(R.string.learn_locked)

    val rows = buildList {
        var i = 0
        Curriculum.modules.forEach { m ->
            add(PathRow.Header(m))
            m.lessons.forEach { add(PathRow.Node(it, i++)) }
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
        item { ScreenHeader(stringResource(R.string.learn_title), subtitle = stringResource(R.string.learn_subtitle)) }
        item { TopCard(state, onOpenLesson, onOpenReview) }
        items(rows, key = { r -> when (r) { is PathRow.Header -> "h_" + r.module.id; is PathRow.Node -> r.lesson.id } }) { row ->
            when (row) {
                is PathRow.Header -> ModuleHeader(row.module, state)
                is PathRow.Node -> {
                    val done = row.lesson.id in state.done
                    val unlocked = vm.isUnlocked(row.lesson.id, state.done)
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        LessonNode(
                            lesson = row.lesson,
                            done = done,
                            unlocked = unlocked,
                            current = unlocked && !done,
                            modifier = Modifier.offset(x = zigzag[row.indexInPath % zigzag.size]),
                            onClick = {
                                haptics.tap()
                                if (unlocked) onOpenLesson(row.lesson.id)
                                else scope.launch { snackbar.showSnackbar(lockedMsg) }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopCard(state: LearnUiState, onOpenLesson: (String) -> Unit, onOpenReview: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    SectionCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalFireDepartment, null, tint = colors.secondary)
                Spacer(Modifier.width(6.dp))
                Text("${state.streak} дн. подряд", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("Сегодня: ${state.todayDone}/${state.dailyGoal}", style = MaterialTheme.typography.titleMedium, color = colors.primary)
            }
            LinearProgressIndicator(
                progress = { state.done.size.toFloat() / state.total },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = colors.secondary,
                trackColor = colors.surfaceVariant,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
            Text("Пройдено уроков: ${state.done.size} из ${state.total}", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            state.nextLesson?.let { next ->
                Button(onClick = { onOpenLesson(next.id) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Icon(Icons.Filled.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        (if (state.done.isEmpty()) "Начать: " else "Продолжить: ") + next.title,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (state.dueReviews > 0) {
                Surface(onClick = onOpenReview, shape = MaterialTheme.shapes.large, color = colors.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Replay, null, tint = colors.onSecondaryContainer)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Повторение ошибок", style = MaterialTheme.typography.titleMedium)
                            Text("Карточек к повторению: ${state.dueReviews}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleHeader(m: LearningModule, state: LearnUiState) {
    val doneCount = m.lessons.count { it.id in state.done }
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (doneCount == m.lessons.size) colors.primary else colors.primaryContainer,
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 8.dp),
    ) {
        val onColor = if (doneCount == m.lessons.size) colors.onPrimary else colors.onPrimaryContainer
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.learn_module_n, m.number) + " · $doneCount/${m.lessons.size}", style = MaterialTheme.typography.labelMedium, color = onColor.copy(alpha = 0.8f))
                Text(stringResource(m.title), style = MaterialTheme.typography.titleLarge, color = onColor)
                Text(stringResource(m.description), style = MaterialTheme.typography.bodySmall, color = onColor.copy(alpha = 0.8f))
            }
            ArabicText(m.symbol, fontSize = 26.sp, color = onColor)
        }
    }
}

@Composable
private fun LessonNode(
    lesson: Lesson,
    done: Boolean,
    unlocked: Boolean,
    current: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val scale = if (current) {
        val pulse = rememberInfiniteTransition(label = "pulse")
        pulse.animateFloat(1f, 1.07f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "s").value
    } else 1f

    Column(modifier.width(180.dp).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = when { done -> colors.secondary; unlocked -> colors.primary; else -> colors.surfaceVariant },
            border = if (current) BorderStroke(4.dp, colors.secondary.copy(alpha = 0.6f)) else null,
            shadowElevation = if (unlocked) 6.dp else 0.dp,
            modifier = Modifier.size(76.dp).scale(scale),
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    done -> Icon(Icons.Filled.Check, null, tint = colors.onSecondary, modifier = Modifier.size(36.dp))
                    unlocked -> Icon(Icons.Filled.PlayArrow, null, tint = colors.onPrimary, modifier = Modifier.size(36.dp))
                    else -> Icon(Icons.Filled.Lock, null, tint = colors.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            lesson.title,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            color = if (unlocked) colors.onBackground else colors.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
