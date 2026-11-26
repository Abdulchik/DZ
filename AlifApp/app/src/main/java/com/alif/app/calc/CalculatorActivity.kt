package com.alif.app.calc

import android.content.Intent
import android.graphics.Color as AColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alif.app.AlifApplication
import com.alif.app.MainActivity
import com.alif.app.data.settings.AppSettings

/**
 * Обычный калькулятор, под которым спрятано приложение.
 * Чтобы войти: набрать код (по умолчанию 786) и зажать кнопку «=».
 */
class CalculatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AColor.TRANSPARENT),
        )
        if (android.os.Build.VERSION.SDK_INT >= 33) setRecentsScreenshotEnabled(false)
        val settingsRepo = (application as AlifApplication).container.settingsRepository
        setContent {
            val settings by settingsRepo.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
            CalculatorScreen(unlockCode = settings.unlockCode, onUnlock = ::openApp)
        }
    }

    private fun openApp() {
        startActivity(Intent(this, MainActivity::class.java).putExtra(MainActivity.EXTRA_UNLOCKED, true))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}

private val Bg = Color(0xFF17171A)
private val Num = Color(0xFF2C2C30)
private val Fn = Color(0xFF4A4A50)
private val Op = Color(0xFFFF9F0A)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalculatorScreen(unlockCode: String, onUnlock: () -> Unit) {
    var expr by remember { mutableStateOf("") }
    var shownResult by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    val preview = remember(expr) {
        if (expr.none { it in Calculator.operators } || shownResult) ""
        else runCatching { Calculator.evaluate(expr)?.let(Calculator::format) }.getOrNull().orEmpty()
    }

    fun input(key: String) {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (expr == "Ошибка") expr = ""
        when (key) {
            "C" -> { expr = ""; shownResult = false }
            "⌫" -> { expr = expr.dropLast(1); shownResult = false }
            "=" -> {
                expr = try {
                    Calculator.evaluate(expr)?.let(Calculator::format) ?: expr
                } catch (e: ArithmeticException) { "Ошибка" }
                shownResult = true
            }
            "%" -> if (expr.isNotEmpty() && (expr.last().isDigit())) expr += "%"
            "." -> {
                val lastNumber = expr.takeLastWhile { it.isDigit() || it == '.' }
                if (shownResult) { expr = "0."; shownResult = false }
                else if ('.' !in lastNumber) expr += if (lastNumber.isEmpty()) "0." else "."
            }
            "+", "−", "×", "÷" -> {
                shownResult = false
                val op = key[0]
                expr = when {
                    expr.isEmpty() -> if (op == Calculator.MINUS) "−" else ""
                    expr.last() in Calculator.operators -> if (expr.length == 1) expr else expr.dropLast(1) + op
                    else -> expr + op
                }
            }
            else -> { // цифры
                if (shownResult) { expr = ""; shownResult = false }
                if (expr.length < 40) expr += key
            }
        }
    }

    /** Долгое нажатие на «=»: если на экране код — открываем приложение. */
    fun longEquals() {
        if (expr == unlockCode && unlockCode.isNotEmpty()) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            expr = ""
            onUnlock()
        } else input("=")
    }

    Surface(color = Bg, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
            Spacer(Modifier.weight(1f))
            Text(
                expr.ifEmpty { "0" },
                color = Color.White,
                fontSize = if (expr.length > 12) 40.sp else 64.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.End,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                preview,
                color = Color(0xFF9A9AA0),
                fontSize = 28.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().height(40.dp),
            )
            Spacer(Modifier.height(16.dp))
            val rows = listOf(
                listOf("C", "⌫", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "−"),
                listOf("1", "2", "3", "+"),
                listOf("00", "0", ".", "="),
            )
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { key ->
                        val color = when (key) {
                            "C", "⌫", "%" -> Fn
                            "÷", "×", "−", "+", "=" -> Op
                            else -> Num
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(color)
                                .combinedClickable(
                                    onClick = { input(key) },
                                    onLongClick = when (key) {
                                        "=" -> ({ longEquals() })
                                        "⌫" -> ({ input("C") })
                                        else -> null
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(key, color = Color.White, fontSize = 30.sp)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
