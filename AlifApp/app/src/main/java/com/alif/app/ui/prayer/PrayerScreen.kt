package com.alif.app.ui.prayer

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.app.data.prayer.CalcMethod
import com.alif.app.data.prayer.Cities
import com.alif.app.data.prayer.NamazGuide
import com.alif.app.data.prayer.Prayer
import com.alif.app.ui.LocalSnackbar
import com.alif.app.ui.appViewModelFactory
import com.alif.app.ui.components.ScreenHeader
import com.alif.app.ui.components.SectionCard
import com.alif.app.ui.components.rememberHaptics
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
private val dateFmt = DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("ru"))

@Composable
fun PrayerScreen(
    onOpenQibla: () -> Unit,
    onOpenGuide: () -> Unit,
    vm: PrayerViewModel = viewModel(factory = appViewModelFactory { PrayerViewModel(it) }),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val locating by vm.locating.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    var showPlace by remember { mutableStateOf(false) }
    var showMethod by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    val onError: (String) -> Unit = { msg -> scope.launch { snackbar.showSnackbar(msg) } }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.locate(onError) else onError("Без доступа к местоположению выберите город из списка")
    }
    fun requestGps() {
        showPlace = false
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            vm.locate(onError)
        } else permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader("Намаз", subtitle = state.now.format(dateFmt).replaceFirstChar { it.uppercase() })

        // Место
        SectionCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp), onClick = { showPlace = true }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, null, tint = colors.secondary)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(state.settings.placeName, style = MaterialTheme.typography.titleMedium)
                    Text("Нажмите, чтобы сменить город или определить по GPS", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                if (locating) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            }
        }

        // Следующий намаз
        state.next?.let { next ->
            Surface(shape = MaterialTheme.shapes.extraLarge, color = colors.primary, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Column(Modifier.padding(22.dp)) {
                    Text("Следующий намаз", style = MaterialTheme.typography.labelLarge, color = colors.onPrimary.copy(alpha = 0.8f))
                    Text(next.title, style = MaterialTheme.typography.headlineMedium, color = colors.onPrimary)
                    val t = state.nextTime
                    if (t != null) {
                        val d = Duration.between(state.now, t)
                        val left = if (d.toHours() > 0) "через ${d.toHours()} ч ${d.toMinutes() % 60} мин" else "через ${d.toMinutes()} мин"
                        Text("${t.format(timeFmt)} · $left", style = MaterialTheme.typography.titleMedium, color = colors.onPrimary)
                    }
                }
            }
        }

        // Время на сегодня
        SectionCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column {
                Prayer.entries.forEachIndexed { i, p ->
                    val isCurrent = p == state.current
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            p.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isCurrent -> colors.primary
                                !p.isPrayer -> colors.onSurfaceVariant
                                else -> colors.onSurface
                            },
                            modifier = Modifier.weight(1f),
                        )
                        if (isCurrent) {
                            Surface(shape = MaterialTheme.shapes.small, color = colors.secondaryContainer) {
                                Text("сейчас", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(
                            state.times[p]?.format(timeFmt) ?: "—",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCurrent) colors.primary else colors.onSurface,
                        )
                    }
                    if (i < Prayer.entries.lastIndex) HorizontalDivider(color = colors.outlineVariant)
                }
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = { showMethod = true }) {
                    Icon(Icons.Filled.Settings, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    val m = runCatching { CalcMethod.valueOf(state.settings.calcMethod) }.getOrDefault(CalcMethod.KARACHI)
                    Text("Расчёт: ${m.title.substringBefore(" (")} · Аср ${if (state.settings.hanafiAsr) "ханафитский" else "шафиитский"}")
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { haptics.tap(); onOpenQibla() }, modifier = Modifier.weight(1f).height(64.dp)) {
                Icon(Icons.Filled.Explore, null)
                Spacer(Modifier.width(8.dp))
                Text("Кибла")
            }
            FilledTonalButton(onClick = { haptics.tap(); onOpenGuide() }, modifier = Modifier.weight(1f).height(64.dp)) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, null)
                Spacer(Modifier.width(8.dp))
                Text("Как читать")
            }
        }

        // Ракаты
        SectionCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Сколько ракатов", style = MaterialTheme.typography.titleLarge)
                NamazGuide.rakats.forEach { row ->
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(row.prayer, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text("${row.parts.sumOf { it.second }}", style = MaterialTheme.typography.titleMedium, color = colors.secondary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.parts.forEach { (kind, n) ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = when (kind) {
                                        "Фард" -> colors.primary
                                        "Витр" -> colors.secondary
                                        else -> colors.primaryContainer
                                    },
                                ) {
                                    Text(
                                        "$n $kind",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = when (kind) {
                                            "Фард" -> colors.onPrimary
                                            "Витр" -> colors.onSecondary
                                            else -> colors.onPrimaryContainer
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    )
                                }
                            }
                        }
                        if (row.note.isNotEmpty()) {
                            Text(row.note, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                    }
                }
                Text(
                    "Фард — обязательный, сунна — по примеру Пророка ﷺ, витр — ваджиб. По ханафитскому мазхабу.",
                    style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant,
                )
            }
        }
    }

    if (showPlace) {
        AlertDialog(
            onDismissRequest = { showPlace = false },
            title = { Text("Где вы находитесь?") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedButton(onClick = ::requestGps, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.MyLocation, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Определить по GPS")
                    }
                    Spacer(Modifier.height(8.dp))
                    Cities.all.forEach { city ->
                        Text(
                            city.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth().clickable { vm.setCity(city); showPlace = false }.padding(vertical = 10.dp),
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPlace = false }) { Text("Закрыть") } },
        )
    }

    if (showMethod) {
        AlertDialog(
            onDismissRequest = { showMethod = false },
            title = { Text("Метод расчёта") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    CalcMethod.entries.forEach { m ->
                        Row(Modifier.fillMaxWidth().clickable { vm.setMethod(m) }, verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(state.settings.calcMethod == m.name, onClick = { vm.setMethod(m) })
                            Text(m.title)
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("Время Асра", style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth().clickable { vm.setHanafiAsr(true) }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(state.settings.hanafiAsr, onClick = { vm.setHanafiAsr(true) })
                        Text("Ханафитский (тень = 2 длины предмета)")
                    }
                    Row(Modifier.fillMaxWidth().clickable { vm.setHanafiAsr(false) }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(!state.settings.hanafiAsr, onClick = { vm.setHanafiAsr(false) })
                        Text("Шафиитский / маликитский / ханбалитский")
                    }
                    Text(
                        "Время может отличаться от мечети на несколько минут. Если в вашей мечети другое расписание — ориентируйтесь на него.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showMethod = false }) { Text("Готово") } },
        )
    }
}
