package com.alif.app.data.prayer

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/** Метод расчёта: углы солнца под горизонтом для Фаджра и Иши. */
enum class CalcMethod(val title: String, val fajr: Double, val isha: Double, val ishaMinutes: Int = 0) {
    KARACHI("Карачи (Университет исламских наук)", 18.0, 18.0),
    MWL("Всемирная исламская лига", 18.0, 17.0),
    EGYPT("Египет", 19.5, 17.5),
    ISNA("Северная Америка (ISNA)", 15.0, 15.0),
    RUSSIA("ДУМ России", 16.0, 15.0),
}

enum class Prayer(val title: String, val isPrayer: Boolean = true) {
    FAJR("Фаджр (Бомдод)"),
    SUNRISE("Восход", isPrayer = false),
    DHUHR("Зухр (Пешин)"),
    ASR("Аср (Дигар)"),
    MAGHRIB("Магриб (Шом)"),
    ISHA("Иша (Хуфтан)"),
}

/**
 * Астрономический расчёт времени намаза (формулы PrayTimes.org).
 * Работает без интернета: нужны только координаты и часовой пояс.
 */
object PrayerCalculator {

    private fun dsin(d: Double) = sin(Math.toRadians(d))
    private fun dcos(d: Double) = cos(Math.toRadians(d))
    private fun dtan(d: Double) = tan(Math.toRadians(d))
    private fun darcsin(x: Double) = Math.toDegrees(asin(x))
    private fun darccos(x: Double) = Math.toDegrees(acos(x))
    private fun darctan2(y: Double, x: Double) = Math.toDegrees(atan2(y, x))
    private fun darccot(x: Double) = Math.toDegrees(atan(1 / x))
    private fun fixAngle(a: Double) = a - 360.0 * floor(a / 360.0)
    private fun fixHour(a: Double) = a - 24.0 * floor(a / 24.0)

    private fun julian(date: LocalDate): Double {
        var y = date.year
        var m = date.monthValue
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + date.dayOfMonth + b - 1524.5
    }

    /** Склонение солнца и уравнение времени. */
    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g))
        val e = 23.439 - 0.00000036 * d
        val ra = darctan2(dcos(e) * dsin(l), dcos(l)) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = darcsin(dsin(e) * dsin(l))
        return decl to eqt
    }

    fun calculate(
        date: LocalDate,
        lat: Double,
        lng: Double,
        zone: ZoneId,
        method: CalcMethod,
        hanafiAsr: Boolean,
    ): Map<Prayer, ZonedDateTime?> {
        val tz = zone.rules.getOffset(date.atTime(12, 0)).totalSeconds / 3600.0
        val jd = julian(date) - lng / (15 * 24)

        fun midDay(t: Double): Double {
            val eqt = sunPosition(jd + t).second
            return fixHour(12 - eqt)
        }

        /** Время, когда солнце на угле [angle] ниже горизонта (ccw — до полудня). */
        fun sunAngleTime(angle: Double, t: Double, ccw: Boolean): Double {
            val decl = sunPosition(jd + t).first
            val noon = midDay(t)
            val x = (-dsin(angle) - dsin(decl) * dsin(lat)) / (dcos(decl) * dcos(lat))
            if (x < -1 || x > 1) return Double.NaN // солнце не опускается так низко (белые ночи)
            val diff = darccos(x) / 15.0
            return noon + if (ccw) -diff else diff
        }

        fun asrTime(factor: Double, t: Double): Double {
            val decl = sunPosition(jd + t).first
            val angle = -darccot(factor + dtan(abs(lat - decl)))
            return sunAngleTime(angle, t, false)
        }

        // Два прохода для точности
        var fajr = 5.0; var sunrise = 6.0; var dhuhr = 12.0; var asr = 13.0; var sunset = 18.0; var isha = 18.0
        fun orDefault(v: Double, d: Double) = if (v.isNaN()) d else v
        repeat(2) {
            fajr = sunAngleTime(method.fajr, orDefault(fajr, 5.0) / 24, true)
            sunrise = sunAngleTime(0.833, orDefault(sunrise, 6.0) / 24, true)
            dhuhr = midDay(dhuhr / 24)
            asr = asrTime(if (hanafiAsr) 2.0 else 1.0, orDefault(asr, 13.0) / 24)
            sunset = sunAngleTime(0.833, orDefault(sunset, 18.0) / 24, false)
            isha = if (method.ishaMinutes > 0) sunset + method.ishaMinutes / 60.0
            else sunAngleTime(method.isha, orDefault(isha, 18.0) / 24, false)
        }

        // Поправка для высоких широт (Москва летом и т.п.): часть ночи по углу
        val night = fixHour(sunrise - sunset)
        if (!night.isNaN()) {
            val fajrMax = night * method.fajr / 60.0
            if (fajr.isNaN() || fixHour(sunrise - fajr) > fajrMax) fajr = sunrise - fajrMax
            val ishaMax = night * method.isha / 60.0
            if (isha.isNaN() || fixHour(isha - sunset) > ishaMax) isha = sunset + ishaMax
        }

        val local = { h: Double -> h + tz - lng / 15.0 }
        fun toTime(h: Double, extraMin: Int = 0): ZonedDateTime? {
            if (h.isNaN()) return null
            val total = fixHour(local(h)) * 60 + extraMin
            val mins = Math.round(total).toInt().mod(24 * 60)
            return ZonedDateTime.of(date, LocalTime.of(mins / 60, mins % 60), zone)
        }
        return mapOf(
            Prayer.FAJR to toTime(fajr),
            Prayer.SUNRISE to toTime(sunrise),
            Prayer.DHUHR to toTime(dhuhr, extraMin = 1), // чуть после зенита
            Prayer.ASR to toTime(asr),
            Prayer.MAGHRIB to toTime(sunset, extraMin = 1),
            Prayer.ISHA to toTime(isha),
        )
    }

    // ---------- Кибла ----------
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LNG = 39.826206

    /** Направление на Каабу в градусах от истинного севера (по часовой стрелке). */
    fun qiblaBearing(lat: Double, lng: Double): Double {
        val dLng = Math.toRadians(KAABA_LNG - lng)
        val f1 = Math.toRadians(lat)
        val f2 = Math.toRadians(KAABA_LAT)
        val y = sin(dLng) * cos(f2)
        val x = cos(f1) * sin(f2) - sin(f1) * cos(f2) * cos(dLng)
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    /** Расстояние до Мекки, км. */
    fun distanceToKaaba(lat: Double, lng: Double): Int {
        val r = 6371.0
        val dLat = Math.toRadians(KAABA_LAT - lat)
        val dLng = Math.toRadians(KAABA_LNG - lng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat)) * cos(Math.toRadians(KAABA_LAT)) * sin(dLng / 2) * sin(dLng / 2)
        return (2 * r * atan2(Math.sqrt(a), Math.sqrt(1 - a))).toInt()
    }
}
