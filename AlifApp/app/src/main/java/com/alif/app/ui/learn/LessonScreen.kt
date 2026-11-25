package com.alif.app.ui.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.app.ui.LocalSnackbar
import com.alif.app.ui.appViewModelFactory
import com.alif.app.ui.components.rememberHaptics

@Composable
fun LessonScreen(lessonId: String, onClose: () -> Unit) {
    val vm: LessonViewModel = viewModel(
        key = "lesson_$lessonId",
        factory = appViewModelFactory { LessonViewModel(it, lessonId) },
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val favorites by vm.favoriteKeys.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbar.current
    val haptics = rememberHaptics()

    LaunchedEffect(vm) { vm.messages.collect { snackbar.showSnackbar(it) } }
    LaunchedEffect(state.answer) {
        state.answer?.let { if (it.correct) haptics.tap() else haptics.strong() }
    }

    val progress by animateFloatAsState(state.progress, label = "progress")

    Column(Modifier.fillMaxSize()) {
        // Верхняя панель: закрыть + прогресс
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, "Закрыть") }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.weight(1f).height(12.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
            Spacer(Modifier.width(16.dp))
        }
        Text(
            when (state.phase) {
                Phase.PAGES -> state.title
                Phase.LISTEN -> "Прослушивание"
                Phase.PRACTICE -> if (state.isReview) "Повторение" else "Упражнения"
                Phase.TEST -> "Мини-тест · ${state.index + 1} из ${state.exercises.size}"
                Phase.RESULT -> "Итог"
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Box(Modifier.weight(1f)) {
            AnimatedContent(
                targetState = Triple(state.phase, state.pageIndex, state.index),
                transitionSpec = {
                    (slideInHorizontally { it / 4 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 4 } + fadeOut())
                },
                label = "lessonContent",
            ) { (phase, pageIndex, exIndex) ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                ) {
                    when (phase) {
                        Phase.PAGES -> state.pages.getOrNull(pageIndex)?.let { LessonPageView(it, vm, favorites) }
                        Phase.LISTEN -> ListenPhase(state.listenItems, vm)
                        Phase.PRACTICE, Phase.TEST -> state.exercises.getOrNull(exIndex)?.let { ExerciseView(it, state, vm) }
                        Phase.RESULT -> ResultView(state, onClose, vm::retryTest)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        // Нижняя панель
        Box(Modifier.fillMaxWidth().padding(16.dp)) {
            when (state.phase) {
                Phase.PAGES -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.pageIndex > 0) {
                        OutlinedButton(onClick = vm::prevPage, modifier = Modifier.height(56.dp)) { Text("Назад") }
                    }
                    Button(onClick = { haptics.tap(); vm.nextPage() }, modifier = Modifier.weight(1f).height(56.dp)) {
                        Text(if (state.pageIndex < state.pages.lastIndex) "Дальше" else "Начать упражнения")
                    }
                }
                Phase.LISTEN -> Button(onClick = { haptics.tap(); vm.finishListening() }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("К упражнениям")
                }
                Phase.PRACTICE, Phase.TEST -> FeedbackArea(state, vm::next)
                Phase.RESULT -> Unit
            }
        }
    }
}

@Composable
private fun ResultView(state: LessonUiState, onClose: () -> Unit, onRetry: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            if (state.passed) Icons.Filled.EmojiEvents else Icons.Filled.Refresh, null,
            modifier = Modifier.size(96.dp),
            tint = if (state.passed) colors.secondary else colors.onSurfaceVariant,
        )
        when {
            state.isReview && state.exercises.isEmpty() -> {
                Text("Нечего повторять", style = MaterialTheme.typography.headlineMedium)
                Text("Ошибок для повторения нет. Отличная работа!", textAlign = TextAlign.Center)
            }
            state.isReview -> {
                Text("Повторение завершено!", style = MaterialTheme.typography.headlineMedium)
                Text("Ошибок: ${state.mistakes}. Карточки вернутся позже — так слова запоминаются надолго.", textAlign = TextAlign.Center)
            }
            state.passed -> {
                Text("Урок пройден!", style = MaterialTheme.typography.headlineMedium)
                Text("Мини-тест: ${state.testCorrect} из ${state.testTotal}. Следующий урок открыт.", textAlign = TextAlign.Center)
            }
            else -> {
                Text("Почти получилось", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Мини-тест: ${state.testCorrect} из ${state.testTotal}. Нужно не меньше 80%. Попробуйте ещё раз — ошибки попали в повторение.",
                    textAlign = TextAlign.Center,
                )
                Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Пройти тест ещё раз") }
            }
        }
        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("К списку уроков") }
    }
}

/** Отдельная функция без ColumnScope — чтобы вызвался обычный AnimatedVisibility. */
@Composable
private fun FeedbackArea(state: LessonUiState, onNext: () -> Unit) {
    val ex = state.current
    AnimatedVisibility(
        visible = state.answer != null && ex != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
    ) {
        val a = state.answer
        if (a != null && ex != null) FeedbackPanel(ex, a, onNext)
    }
}
