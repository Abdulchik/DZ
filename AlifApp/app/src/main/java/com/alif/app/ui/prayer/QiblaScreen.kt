package com.alif.app.ui.prayer

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.app.data.prayer.PrayerCalculator
import com.alif.app.ui.appViewModelFactory
import com.alif.app.ui.components.rememberHaptics
import com.alif.app.ui.theme.Correct
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    onBack: () -> Unit,
    vm: PrayerViewModel = viewModel(factory = appViewModelFactory { PrayerViewModel(it) }),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val s = state.settings
    val qibla = PrayerCalculator.qiblaBearing(s.placeLat, s.placeLng).toFloat()
    val distance = PrayerCalculator.distanceToKaaba(s.placeLat, s.placeLng)
    val context = LocalContext.current
    val haptics = rememberHaptics()

    var heading by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(true) }
    var accuracyLow by remember { mutableStateOf(false) }

    // Компас: датчик поворота → азимут; поправка на магнитное склонение → истинный север
    DisposableEffect(s.placeLat, s.placeLng) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotation = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnet = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val declination = GeomagneticField(s.placeLat.toFloat(), s.placeLng.toFloat(), 0f, System.currentTimeMillis()).declination
        val r = FloatArray(9)
        val orientation = FloatArray(3)
        val g = FloatArray(3)
        val m = FloatArray(3)
        var hasG = false
        var hasM = false

        fun update(azimuthRad: Float) {
            val deg = (Math.toDegrees(azimuthRad.toDouble()).toFloat() + declination + 360f) % 360f
            // Плавность: сглаживаем, учитывая переход через 0°/360°
            var diff = deg - heading
            if (diff > 180) diff -= 360
            if (diff < -180) diff += 360
            heading = (heading + diff * 0.15f + 360f) % 360f
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                when (e.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(r, e.values)
                        SensorManager.getOrientation(r, orientation)
                        update(orientation[0])
                    }
                    Sensor.TYPE_ACCELEROMETER -> { System.arraycopy(e.values, 0, g, 0, 3); hasG = true }
                    Sensor.TYPE_MAGNETIC_FIELD -> { System.arraycopy(e.values, 0, m, 0, 3); hasM = true }
                }
                if (rotation == null && hasG && hasM && SensorManager.getRotationMatrix(r, null, g, m)) {
                    SensorManager.getOrientation(r, orientation)
                    update(orientation[0])
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD || sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    accuracyLow = accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW
                }
            }
        }
        if (rotation != null) {
            sm.registerListener(listener, rotation, SensorManager.SENSOR_DELAY_UI)
        } else if (accel != null && magnet != null) {
            sm.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
            sm.registerListener(listener, magnet, SensorManager.SENSOR_DELAY_UI)
        } else {
            hasSensor = false
        }
        onDispose { sm.unregisterListener(listener) }
    }

    var delta = (qibla - heading + 360f) % 360f
    if (delta > 180) delta -= 360f
    val aligned = hasSensor && abs(delta) < 5f
    LaunchedEffect(aligned) { if (aligned) haptics.strong() }

    val colors = MaterialTheme.colorScheme
    val ring by animateColorAsState(if (aligned) Correct else colors.outline, label = "ring")

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Кибла") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
            windowInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal),
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                when {
                    !hasSensor -> "В телефоне нет компаса"
                    aligned -> "Вы смотрите на Киблу"
                    delta > 0 -> "Поверните вправо на ${delta.toInt()}°"
                    else -> "Поверните влево на ${(-delta).toInt()}°"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = if (aligned) Correct else colors.onBackground,
                textAlign = TextAlign.Center,
            )

            Box(Modifier.fillMaxWidth(0.9f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                val northColor = colors.error
                val tick = colors.onSurfaceVariant
                val gold = colors.secondary
                val face = colors.surfaceContainerLowest
                val textColor = colors.onSurface.toArgb()
                Canvas(Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    val c = center
                    drawCircle(face, radius)
                    drawCircle(ring, radius, style = Stroke(width = 6.dp.toPx()))
                    // Шкала вращается против поворота телефона: север всегда на севере
                    rotate(-heading, c) {
                        for (i in 0 until 72) {
                            val long = i % 9 == 0
                            val len = if (long) 18.dp.toPx() else 8.dp.toPx()
                            rotate(i * 5f, c) {
                                drawLine(tick, Offset(c.x, c.y - radius + 6.dp.toPx()), Offset(c.x, c.y - radius + 6.dp.toPx() + len), strokeWidth = if (long) 3f else 1.5f)
                            }
                        }
                        val paint = android.graphics.Paint().apply {
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 22.dp.toPx()
                            isFakeBoldText = true
                        }
                        listOf("С" to 0f, "В" to 90f, "Ю" to 180f, "З" to 270f).forEach { (label, angle) ->
                            rotate(angle, c) {
                                paint.color = if (label == "С") northColor.toArgb() else textColor
                                drawContext.canvas.nativeCanvas.drawText(label, c.x, c.y - radius + 48.dp.toPx(), paint)
                            }
                        }
                        // Стрелка на Каабу
                        rotate(qibla, c) {
                            val tip = Offset(c.x, c.y - radius + 60.dp.toPx())
                            val path = Path().apply {
                                moveTo(tip.x, tip.y)
                                lineTo(c.x - 18.dp.toPx(), c.y)
                                lineTo(c.x + 18.dp.toPx(), c.y)
                                close()
                            }
                            drawPath(path, if (aligned) Correct else gold)
                            // «Кааба» — чёрный квадрат с золотой полосой
                            val kSize = 26.dp.toPx()
                            drawRect(Color(0xFF1A1A1A), Offset(tip.x - kSize / 2, tip.y - kSize - 6.dp.toPx()), androidx.compose.ui.geometry.Size(kSize, kSize))
                            drawRect(gold, Offset(tip.x - kSize / 2, tip.y - kSize + 2.dp.toPx()), androidx.compose.ui.geometry.Size(kSize, 3.dp.toPx()))
                        }
                    }
                    // Неподвижная метка «верх телефона»
                    drawCircle(colors.primary, 8.dp.toPx(), Offset(c.x, c.y - radius - 2.dp.toPx()))
                    drawCircle(colors.primary, 10.dp.toPx(), c)
                }
            }

            Text("Направление: ${qibla.toInt()}° от севера · до Мекки $distance км", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(
                "Держите телефон горизонтально, верхом от себя. Поворачивайтесь, пока стрелка с Каабой не окажется вверху и круг не станет зелёным.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (accuracyLow) {
                Text(
                    "Компас неточный: подвигайте телефоном восьмёркой в воздухе и отойдите от металла и электроприборов.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.error,
                    textAlign = TextAlign.Center,
                )
            }
            Text("Место: ${s.placeName}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}
