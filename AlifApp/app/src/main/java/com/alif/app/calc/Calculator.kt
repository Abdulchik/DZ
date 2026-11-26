package com.alif.app.calc

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Простой и надёжный калькулятор: + − × ÷ и %, с приоритетом умножения/деления.
 * Работает на BigDecimal — без ошибок вроде 0.1 + 0.2 = 0.30000000000000004.
 */
object Calculator {
    const val PLUS = '+'
    const val MINUS = '−'
    const val TIMES = '×'
    const val DIV = '÷'
    val operators = setOf(PLUS, MINUS, TIMES, DIV)

    private val mc = MathContext(16, RoundingMode.HALF_UP)

    /** @return результат или null, если выражение неполное; бросает ArithmeticException при делении на 0. */
    fun evaluate(expr: String): BigDecimal? {
        if (expr.isEmpty()) return null
        val tokens = tokenize(expr) ?: return null
        // Сначала × и ÷, потом + и −
        val stack = ArrayList<Any>()
        var i = 0
        while (i < tokens.size) {
            val t = tokens[i]
            if (t is Char && (t == TIMES || t == DIV)) {
                val left = stack.removeAt(stack.lastIndex) as BigDecimal
                val right = tokens.getOrNull(i + 1) as? BigDecimal ?: return null
                stack.add(if (t == TIMES) left.multiply(right, mc) else left.divide(right, mc))
                i += 2
            } else {
                stack.add(t)
                i++
            }
        }
        var result = stack.firstOrNull() as? BigDecimal ?: return null
        var j = 1
        while (j < stack.size) {
            val op = stack[j] as Char
            val v = stack.getOrNull(j + 1) as? BigDecimal ?: return null
            result = if (op == PLUS) result.add(v, mc) else result.subtract(v, mc)
            j += 2
        }
        return result
    }

    private fun tokenize(expr: String): List<Any>? {
        val out = ArrayList<Any>()
        val num = StringBuilder()
        fun flush(): Boolean {
            if (num.isEmpty()) return true
            var s = num.toString()
            var percent = false
            if (s.endsWith("%")) { percent = true; s = s.dropLast(1) }
            if (s == "-" || s.isEmpty() || s == ".") return false
            var v = s.toBigDecimalOrNull() ?: return false
            if (percent) v = v.divide(BigDecimal(100), mc)
            out.add(v)
            num.clear()
            return true
        }
        for ((idx, c) in expr.withIndex()) {
            when {
                c.isDigit() || c == '.' || c == '%' -> num.append(c)
                c == MINUS && (idx == 0 || expr[idx - 1] in operators) -> num.append('-') // отрицательное число
                c in operators -> {
                    if (!flush()) return null
                    out.add(c)
                }
            }
        }
        if (!flush()) return null
        if (out.isEmpty() || out.last() is Char) return null
        return out
    }

    fun format(v: BigDecimal): String {
        val s = v.round(MathContext(12, RoundingMode.HALF_UP)).stripTrailingZeros()
        val plain = s.toPlainString()
        return if (plain.length > 14) s.toString().replace("E+", "e") else plain
    }
}
