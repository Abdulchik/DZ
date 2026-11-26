package com.alif.app.ui.profile

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.app.R
import com.alif.app.data.quran.Reciters
import com.alif.app.data.settings.AppSettings
import com.alif.app.data.settings.ThemeMode
import com.alif.app.ui.appViewModelFactory
import com.alif.app.ui.components.ArabicText
import com.alif.app.ui.components.ListRow
import com.alif.app.ui.components.ScreenHeader
import com.alif.app.ui.components.SectionCard
import com.alif.app.ui.components.rememberHaptics
import com.alif.app.util.AppLanguage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenAbout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = appViewModelFactory { ProfileViewModel(it) }),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    var showTimePicker by remember { mutableStateOf(false) }
    var showReciters by remember { mutableStateOf(false) }
    var showCode by remember { mutableStateOf(false) }

    // Разрешение на уведомления (Android 13+)
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.setReminder(true, settings.reminderHour, settings.reminderMinute)
    }
    fun enableReminder(h: Int, m: Int) {
        if (Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        viewModel.setReminder(true, h, m)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenHeader(stringResource(R.string.profile_title))

        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(Icons.Filled.LocalFireDepartment, "${stats.streak}", stringResource(R.string.profile_streak), Modifier.weight(1f))
            StatCard(Icons.Filled.School, "${stats.lessonsDone}", stringResource(R.string.profile_lessons_done), Modifier.weight(1f))
            StatCard(Icons.Filled.CheckCircle, "${stats.percent}%", stringResource(R.string.profile_learned_percent), Modifier.weight(1f))
        }

        // Неделя: в какие дни занимались
        SettingsCard {
            Text("Эта неделя", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            val monday = LocalDate.now().with(DayOfWeek.MONDAY)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (0..6).forEach { i ->
                    val day = monday.plusDays(i.toLong())
                    val active = day.toString() in stats.activeDays
                    val isToday = day == LocalDate.now()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru")),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (active) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(top = 4.dp).size(30.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (active) Icon(Icons.Filled.LocalFireDepartment, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Ежедневная цель и напоминание
        SettingsCard {
            ListRow(Icons.Filled.Flag, "Ежедневная цель", subtitle = "Сегодня: ${stats.today} из ${settings.dailyGoal} уроков")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3, 5).forEach { n ->
                    FilterChip(selected = settings.dailyGoal == n, onClick = { haptics.tap(); viewModel.setGoal(n) }, label = { Text("$n в день") })
                }
            }
            Spacer(Modifier.height(12.dp))
            ListRow(
                Icons.Filled.Notifications, "Напоминание",
                subtitle = if (settings.reminderEnabled) "Каждый день в %02d:%02d".format(settings.reminderHour, settings.reminderMinute) else "Выключено",
                trailing = {
                    Switch(settings.reminderEnabled, onCheckedChange = { on ->
                        if (on) enableReminder(settings.reminderHour, settings.reminderMinute)
                        else viewModel.setReminder(false, settings.reminderHour, settings.reminderMinute)
                    })
                },
            )
            if (settings.reminderEnabled) {
                TextButton(onClick = { showTimePicker = true }) { Text("Изменить время") }
            }
        }

        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))

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
                        icon = {},
                    ) { Text(stringResource(label), maxLines = 1) }
                }
            }
        }

        SettingsCard {
            ListRow(Icons.Filled.TextFields, stringResource(R.string.settings_arabic_size), trailing = {
                Text("${settings.arabicFontSize.toInt()}", style = MaterialTheme.typography.titleMedium)
            })
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
            )
        }

        SettingsCard(onClick = { showReciters = true }) {
            ListRow(Icons.Filled.Headphones, stringResource(R.string.settings_reciter), subtitle = Reciters.byFolder(settings.defaultReciter).name, trailing = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
            })
        }

        SettingsCard {
            ListRow(Icons.Filled.Vibration, stringResource(R.string.settings_haptics), trailing = {
                Switch(settings.hapticsEnabled, onCheckedChange = { viewModel.setHaptics(it) })
            })
            Spacer(Modifier.height(12.dp))
            ListRow(Icons.AutoMirrored.Filled.VolumeUp, stringResource(R.string.settings_sounds), trailing = {
                Switch(settings.soundsEnabled, onCheckedChange = { haptics.tap(); viewModel.setSounds(it) })
            })
        }

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
                            Text(if (lang.available) lang.nativeName else "${lang.nativeName} · ${stringResource(R.string.settings_language_soon)}")
                        },
                    )
                }
            }
        }

        SettingsCard(onClick = { showCode = true }) {
            ListRow(
                Icons.Filled.Lock, "Вход через калькулятор",
                subtitle = "Наберите код в калькуляторе и зажмите «=». Код: ${"•".repeat(settings.unlockCode.length)}",
                trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
            )
        }

        SettingsCard(onClick = { haptics.tap(); onOpenAbout() }) {
            ListRow(Icons.Filled.Info, stringResource(R.string.settings_about), trailing = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
            })
        }
    }

    if (showTimePicker) {
        val tp = rememberTimePickerState(settings.reminderHour, settings.reminderMinute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Время напоминания") },
            text = { TimePicker(tp) },
            confirmButton = {
                TextButton(onClick = { enableReminder(tp.hour, tp.minute); showTimePicker = false }) { Text("Готово") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Отмена") } },
        )
    }

    if (showCode) {
        var code by remember { mutableStateOf("") }
        val valid = code.length in 3..8 && code.all { it.isDigit() }
        AlertDialog(
            onDismissRequest = { showCode = false },
            title = { Text("Новый код входа") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("От 3 до 8 цифр. Чтобы открыть приложение: наберите код в калькуляторе и зажмите кнопку «=».")
                    OutlinedTextField(
                        value = code,
                        onValueChange = { v -> code = v.filter { it.isDigit() }.take(8) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        placeholder = { Text("например, 2580") },
                    )
                }
            },
            confirmButton = {
                TextButton(enabled = valid, onClick = { viewModel.setUnlockCode(code); showCode = false }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { showCode = false }) { Text("Отмена") } },
        )
    }

    if (showReciters) {
        AlertDialog(
            onDismissRequest = { showReciters = false },
            title = { Text(stringResource(R.string.settings_reciter)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Reciters.all.forEach { r ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(settings.defaultReciter == r.folder, onClick = { viewModel.setReciter(r.folder) })
                            Column {
                                Text(r.name)
                                if (r.note.isNotEmpty()) Text(r.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showReciters = false }) { Text("Готово") } },
        )
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
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}
