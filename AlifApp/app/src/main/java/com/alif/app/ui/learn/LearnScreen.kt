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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alif.app.R
import com.alif.app.data.learn.LearningModule
import com.alif.app.data.learn.LearningPath
import com.alif.app.ui.LocalSnackbar
import com.alif.app.ui.components.ArabicText
import com.alif.app.ui.components.ScreenHeader
import com.alif.app.ui.components.SectionCard
import com.alif.app.ui.components.rememberHaptics
import kotlinx.coroutines.launch

/** Сдвиг кружков влево-вправо — получается «змейка», как в Duolingo. */
private val zigzag = listOf(0.dp, 56.dp, 0.dp, (-56).dp)

@Composable
fun LearnScreen() {
    // На этапе 1 открыт только первый модуль; прогресс подключается на этапе 2.
    val unlockedCount = 1
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val lockedMsg = stringResource(R.string.learn_locked)
    val soonMsg = stringResource(R.string.coming_next_stage)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            ScreenHeader(
                title = stringResource(R.string.learn_title),
                subtitle = stringResource(R.string.learn_subtitle),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item { OverallProgressCard(done = 0, total = LearningPath.modules.sumOf { it.lessonCount }) }
        item { Spacer(Modifier.height(24.dp)) }
        itemsIndexed(LearningPath.modules, key = { _, m -> m.id }) { index, module ->
            val unlocked = index < unlockedCount
            ModuleNode(
                module = module,
                unlocked = unlocked,
                isCurrent = index == unlockedCount - 1,
                modifier = Modifier.offset(x = zigzag[index % zigzag.size]),
                onClick = {
                    haptics.tap()
                    scope.launch { snackbar.showSnackbar(if (unlocked) soonMsg else lockedMsg) }
                },
            )
        }
    }
}

@Composable
private fun OverallProgressCard(done: Int, total: Int) {
    SectionCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.profile_lessons_done),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text("$done / $total", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else done.toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
        }
    }
}

@Composable
private fun ModuleNode(
    module: LearningModule,
    unlocked: Boolean,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    // Текущий модуль мягко «дышит», чтобы было понятно, куда нажимать
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (isCurrent) 1.06f else 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "scale",
    )
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier.width(220.dp).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = if (unlocked) colors.primary else colors.surfaceVariant,
            border = if (isCurrent) BorderStroke(4.dp, colors.secondary) else null,
            shadowElevation = if (unlocked) 6.dp else 0.dp,
            modifier = Modifier.size(92.dp).scale(scale),
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (unlocked) {
                    ArabicText(module.symbol, fontSize = 26.sp, color = colors.onPrimary, textAlign = TextAlign.Center)
                } else {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = colors.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.learn_module_n, module.number),
            style = MaterialTheme.typography.labelMedium,
            color = colors.secondary,
        )
        Text(
            stringResource(module.title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = if (unlocked) colors.onBackground else colors.onSurfaceVariant,
        )
        Text(
            stringResource(module.description),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = colors.onSurfaceVariant,
        )
    }
}
