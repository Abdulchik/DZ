package com.alif.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.app.R
import com.alif.app.data.settings.AppSettings
import com.alif.app.data.settings.ThemeMode
import com.alif.app.ui.components.ArabicText
import com.alif.app.ui.components.ListRow
import com.alif.app.ui.components.ScreenHeader
import com.alif.app.ui.components.SectionCard
import com.alif.app.ui.components.rememberHaptics
import com.alif.app.util.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenAbout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenHeader(stringResource(R.string.profile_title))

        // Прогресс (реальные цифры — этап 8)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(Icons.Filled.LocalFireDepartment, "0", stringResource(R.string.profile_streak), Modifier.weight(1f))
            StatCard(Icons.Filled.School, "0", stringResource(R.string.profile_lessons_done), Modifier.weight(1f))
            StatCard(Icons.Filled.CheckCircle, "0%", stringResource(R.string.profile_learned_percent), Modifier.weight(1f))
        }

        Text(
            stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )

        // Тема
        SettingsCard {
            ListRow(Icons.Filled.Palette, stringResource(R.string.settings_theme))
            Spacer(Modifier.height(12.dp))
            val options = listOf(
                ThemeMode.SYSTEM to R.string.theme_system,
                ThemeMode.LIGHT to R.string.theme_light,
                ThemeMode.DARK to R.string.theme_dark,
            )
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                options.forEachIndexed { i, (mode, label) ->
                    SegmentedButton(
                        selected = settings.themeMode == mode,
                        onClick = { haptics.tap(); viewModel.setTheme(mode) },
                        shape = SegmentedButtonDefaults.itemShape(i, options.size),
                    ) { Text(stringResource(label), maxLines = 1) }
                }
            }
        }

        // Размер арабского текста — ползунок с живым предпросмотром
        SettingsCard {
            ListRow(Icons.Filled.TextFields, stringResource(R.string.settings_arabic_size), trailing = {
                Text("${settings.arabicFontSize.toInt()}", style = MaterialTheme.typography.titleMedium)
            })
            // Локальное значение, чтобы ползунок двигался плавно, а в DataStore писалось по отпусканию
            var sliderValue by remember(settings.arabicFontSize) { mutableFloatStateOf(settings.arabicFontSize) }
            ArabicText(
                stringResource(R.string.settings_arabic_preview),
                quran = true,
                fontSize = sliderValue.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
            )
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { haptics.tap(); viewModel.setArabicSize(sliderValue) },
                valueRange = AppSettings.MIN_ARABIC_SIZE..AppSettings.MAX_ARABIC_SIZE,
                steps = ((AppSettings.MAX_ARABIC_SIZE - AppSettings.MIN_ARABIC_SIZE) / 2).toInt() - 1,
            )
        }

        // Звук и вибрация
        SettingsCard {
            ListRow(Icons.Filled.Vibration, stringResource(R.string.settings_haptics), trailing = {
                Switch(settings.hapticsEnabled, onCheckedChange = { viewModel.setHaptics(it) })
            })
            Spacer(Modifier.height(12.dp))
            ListRow(Icons.AutoMirrored.Filled.VolumeUp, stringResource(R.string.settings_sounds), trailing = {
                Switch(settings.soundsEnabled, onCheckedChange = { haptics.tap(); viewModel.setSounds(it) })
            })
        }

        // Язык интерфейса
        SettingsCard {
            ListRow(Icons.Filled.Translate, stringResource(R.string.settings_language))
            Spacer(Modifier.height(8.dp))
            var current by remember { mutableStateOf(AppLanguage.current()) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { lang ->
                    FilterChip(
                        selected = current == lang,
                        enabled = lang.available,
                        onClick = { current = lang; AppLanguage.apply(lang) },
                        label = {
                            Text(
                                if (lang.available) lang.nativeName
                                else "${lang.nativeName} · ${stringResource(R.string.settings_language_soon)}"
                            )
                        },
                    )
                }
            }
        }

        SettingsCard(onClick = { haptics.tap(); onOpenAbout() }) {
            ListRow(Icons.Filled.Info, stringResource(R.string.settings_about), trailing = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
            })
        }
    }
}

@Composable
private fun SettingsCard(onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    SectionCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp), onClick = onClick) {
        Column { content() }
    }
}

@Composable
private fun StatCard(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    SectionCard(modifier, contentPadding = PaddingValues(14.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.secondary)
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
