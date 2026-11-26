package com.alif.app.ui.prayer

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.app.data.prayer.Dua
import com.alif.app.data.prayer.GuideSection
import com.alif.app.data.prayer.NamazGuide
import com.alif.app.data.prayer.NamazStep
import com.alif.app.data.quran.Ayah
import com.alif.app.ui.appViewModelFactory
import com.alif.app.ui.components.ArabicText
import com.alif.app.ui.theme.LocalAppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamazGuideScreen(
    onBack: () -> Unit,
    vm: PrayerViewModel = viewModel(factory = appViewModelFactory { PrayerViewModel(it) }),
) {
    LaunchedEffect(Unit) { vm.loadGuideSurahs() }
    val fatiha by vm.fatiha.collectAsStateWithLifecycle()
    val ikhlas by vm.ikhlas.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Как совершать намаз") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            windowInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal),
        )
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(
                    "По ханафитскому мазхабу. Нажмите на раздел, чтобы раскрыть шаги. Тексты: арабский, чтение и перевод. " +
                        "Стиль чтения (с удлинениями или простой) — как в настройках Корана.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(NamazGuide.sections, key = { it.title }) { section ->
                SectionView(section, expandedByDefault = section == NamazGuide.sections[1], fatiha, ikhlas)
            }
            item {
                Text(
                    "Это краткое руководство для начинающих. По спорным вопросам обращайтесь к имаму своей мечети.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionView(
    section: GuideSection,
    expandedByDefault: Boolean,
    fatiha: List<Pair<Ayah, String>>,
    ikhlas: List<Pair<Ayah, String>>,
) {
    var expanded by rememberSaveable(section.title) { mutableStateOf(expandedByDefault) }
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = { expanded = !expanded },
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth().animateContentSize(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(section.title, style = MaterialTheme.typography.titleLarge)
                    Text(section.intro, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null)
            }
            if (expanded) {
                section.steps.forEach { StepView(it, fatiha, ikhlas) }
            }
        }
    }
}

@Composable
private fun StepView(step: NamazStep, fatiha: List<Pair<Ayah, String>>, ikhlas: List<Pair<Ayah, String>>) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(step.title, style = MaterialTheme.typography.titleMedium, color = colors.primary)
        Text(step.text, style = MaterialTheme.typography.bodyLarge)
        when (step.quranSurah) {
            1 -> SurahBlock(fatiha)
            112 -> SurahBlock(ikhlas)
        }
        step.duas.forEach { DuaCard(it) }
    }
}

@Composable
private fun translitFor(tj: String): String {
    val style = LocalAppSettings.current.translitStyle
    return if (style == "tj") tj else NamazGuide.simplify(tj)
}

@Composable
private fun DuaCard(d: Dua) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = MaterialTheme.shapes.medium, color = colors.secondaryContainer.copy(alpha = 0.45f), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ArabicText(d.arabic, scale = 0.95f, modifier = Modifier.fillMaxWidth())
            Text(translitFor(d.translit), style = MaterialTheme.typography.titleMedium, color = colors.primary)
            Text(d.meaning, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun SurahBlock(ayahs: List<Pair<Ayah, String>>) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = MaterialTheme.shapes.medium, color = colors.primaryContainer.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ayahs.forEach { (a, tl) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ArabicText(a.arabic, quran = true, scale = 0.9f, modifier = Modifier.fillMaxWidth())
                    Text("${a.number}. $tl", style = MaterialTheme.typography.titleMedium, color = colors.primary)
                    Text(a.translation, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}
