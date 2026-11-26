package com.alif.app.ui.prayer

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alif.app.AppContainer
import com.alif.app.data.prayer.CalcMethod
import com.alif.app.data.prayer.City
import com.alif.app.data.prayer.Prayer
import com.alif.app.data.prayer.PrayerCalculator
import com.alif.app.data.quran.Ayah
import com.alif.app.data.settings.AppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

data class PrayerUiState(
    val settings: AppSettings = AppSettings(),
    val times: Map<Prayer, ZonedDateTime?> = emptyMap(),
    val now: ZonedDateTime = ZonedDateTime.now(),
    val next: Prayer? = null,
    val nextTime: ZonedDateTime? = null,
    val current: Prayer? = null,
) {
    val zone: ZoneId get() = zoneOf(settings)
}

fun zoneOf(s: AppSettings): ZoneId =
    runCatching { if (s.placeZone.isBlank()) ZoneId.systemDefault() else ZoneId.of(s.placeZone) }.getOrDefault(ZoneId.systemDefault())

class PrayerViewModel(private val c: AppContainer) : ViewModel() {

    private val ticker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30_000)
        }
    }

    val state: StateFlow<PrayerUiState> = combine(c.settingsRepository.settings, ticker) { s, _ ->
        val zone = zoneOf(s)
        val now = ZonedDateTime.now(zone)
        val method = runCatching { CalcMethod.valueOf(s.calcMethod) }.getOrDefault(CalcMethod.KARACHI)
        val today = PrayerCalculator.calculate(now.toLocalDate(), s.placeLat, s.placeLng, zone, method, s.hanafiAsr)
        val prayers = today.filterKeys { it.isPrayer }
        var next = prayers.entries.firstOrNull { it.value?.isAfter(now) == true }
        if (next == null) {
            // После Иши следующий — завтрашний Фаджр
            val tomorrow = PrayerCalculator.calculate(now.toLocalDate().plusDays(1), s.placeLat, s.placeLng, zone, method, s.hanafiAsr)
            next = tomorrow.entries.first { it.key == Prayer.FAJR }
        }
        val current = today.entries.lastOrNull { it.value?.isBefore(now) == true }?.key
            ?.takeIf { it != Prayer.SUNRISE } ?: if (next.key == Prayer.FAJR) Prayer.ISHA else null
        PrayerUiState(s, today, now, next.key, next.value, current)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrayerUiState())

    /** Фатиха и Ихлас — для руководства по намазу (текст и чтение из Корана). */
    val fatiha = MutableStateFlow<List<Pair<Ayah, String>>>(emptyList())
    val ikhlas = MutableStateFlow<List<Pair<Ayah, String>>>(emptyList())

    val locating = MutableStateFlow(false)

    fun loadGuideSurahs() = viewModelScope.launch {
        val style = c.settingsRepository.current().translitStyle
        suspend fun load(n: Int): List<Pair<Ayah, String>> {
            val ayahs = c.quranRepository.ayahs(n)
            val tl = if (style == "tj") ayahs.map { it.transliteration } else c.quranRepository.transliteration(style, n)
            return ayahs.mapIndexed { i, a -> a to tl.getOrElse(i) { a.transliteration } }
        }
        fatiha.value = load(1)
        ikhlas.value = load(112)
    }

    fun setCity(city: City) = viewModelScope.launch {
        c.settingsRepository.setPlace(city.name, city.lat, city.lng, city.zone)
    }

    fun setMethod(m: CalcMethod) = viewModelScope.launch { c.settingsRepository.setCalcMethod(m.name) }
    fun setHanafiAsr(v: Boolean) = viewModelScope.launch { c.settingsRepository.setHanafiAsr(v) }

    private fun saveLocation(l: Location) = viewModelScope.launch {
        c.settingsRepository.setPlace("Моё местоположение", l.latitude, l.longitude, "")
        locating.value = false
    }

    /** Определить координаты по GPS/сети. Разрешение проверяется на экране. */
    @SuppressLint("MissingPermission")
    fun locate(onError: (String) -> Unit) {
        val lm = c.appContext.getSystemService(LocationManager::class.java)
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
        if (providers.isEmpty()) {
            onError("Включите геолокацию в настройках телефона или выберите город из списка")
            return
        }
        val last = providers.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }.maxByOrNull { it.time }
        if (last != null && System.currentTimeMillis() - last.time < 6 * 60 * 60 * 1000L) {
            saveLocation(last)
            return
        }
        locating.value = true
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                lm.removeUpdates(this)
                saveLocation(location)
            }
            // На Android 8–10 эти методы обязательны
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
        }
        runCatching { lm.requestLocationUpdates(providers.first(), 0L, 0f, listener, Looper.getMainLooper()) }
            .onFailure {
                locating.value = false
                if (last != null) saveLocation(last) else onError("Не удалось определить место. Выберите город из списка")
            }
        // Если за 20 секунд не нашли — берём последнее известное или сообщаем
        viewModelScope.launch {
            delay(20_000)
            if (locating.value) {
                lm.removeUpdates(listener)
                locating.value = false
                if (last != null) saveLocation(last) else onError("Не удалось определить место. Выберите город из списка")
            }
        }
    }
}
