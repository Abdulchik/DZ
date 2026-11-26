package com.alif.app.ui.quran

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alif.app.AppContainer
import com.alif.app.data.audio.QuranPlayer
import com.alif.app.data.db.AyahNote
import com.alif.app.data.db.FavoriteAyah
import com.alif.app.data.db.FavoriteSurah
import com.alif.app.data.quran.Ayah
import com.alif.app.data.quran.Reciters
import com.alif.app.data.quran.Surah
import com.alif.app.data.settings.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReaderViewModel(private val c: AppContainer, val surahNumber: Int) : ViewModel() {

    val surah = MutableStateFlow<Surah?>(null)
    val ayahs = MutableStateFlow<List<Ayah>>(emptyList())

    val settings: StateFlow<AppSettings> = c.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val favoriteAyahs: StateFlow<Set<Int>> = c.database.favorites().ayahsOfSurah(surahNumber)
        .map { l -> l.map { it.ayah }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val isFavoriteSurah: StateFlow<Boolean> = c.database.favorites().surahs()
        .map { l -> l.any { it.surah == surahNumber } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val notes: StateFlow<Map<Int, String>> = c.database.notes().ofSurah(surahNumber)
        .map { l -> l.associate { it.ayah to it.text } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val player: StateFlow<QuranPlayer.State> = c.quranPlayer.state

    /** Таджикский и английский переводы — загружаются, только когда включены. */
    val tajik = MutableStateFlow<List<String>>(emptyList())
    val english = MutableStateFlow<List<String>>(emptyList())

    /** Сколько раз повторять каждый аят при прослушивании. */
    val repeatCount = MutableStateFlow(1)

    init {
        viewModelScope.launch {
            surah.value = c.quranRepository.surah(surahNumber)
            ayahs.value = c.quranRepository.ayahs(surahNumber)
        }
        viewModelScope.launch {
            settings.collect { st ->
                if (st.showTajik && tajik.value.isEmpty()) tajik.value = c.quranRepository.translation("tg", surahNumber)
                if (st.showEnglish && english.value.isEmpty()) english.value = c.quranRepository.translation("en", surahNumber)
            }
        }
    }

    // ---------- Аудио ----------
    fun playFrom(ayah: Int) {
        val s = surah.value ?: return
        val st = settings.value
        c.assetAudio.stop()
        c.quranPlayer.play(
            reciter = Reciters.byFolder(st.defaultReciter).folder,
            surah = surahNumber,
            fromAyah = ayah,
            toAyah = s.ayahCount,
            repeat = repeatCount.value,
            withBismillah = ayah == 1 && s.hasBismillahHeader,
            speed = st.playbackSpeed,
        )
    }

    fun repeatAyah(ayah: Int, times: Int) {
        val st = settings.value
        repeatCount.value = times
        c.quranPlayer.play(Reciters.byFolder(st.defaultReciter).folder, surahNumber, ayah, ayah, repeat = times, speed = st.playbackSpeed)
    }

    fun togglePause() {
        val p = player.value
        if (p.surah != surahNumber || !p.active) playFrom(1) else c.quranPlayer.togglePause()
    }

    fun stop() = c.quranPlayer.stop()
    fun skip(forward: Boolean) = c.quranPlayer.skip(forward)

    fun setSpeed(speed: Float) = viewModelScope.launch {
        c.settingsRepository.setPlaybackSpeed(speed)
        c.quranPlayer.setSpeed(speed)
    }

    fun setRepeat(n: Int) {
        repeatCount.value = n
        // Применяем к текущему прослушиванию — начинаем заново с текущего аята
        val p = player.value
        if (p.active && p.surah == surahNumber && p.ayah > 0) playFrom(p.ayah)
    }

    fun setReciter(folder: String) = viewModelScope.launch {
        c.settingsRepository.setDefaultReciter(folder)
        val p = player.value
        if (p.active && p.surah == surahNumber) {
            // небольшая задержка не нужна: settings обновится через поток, передаём явно
            val s = surah.value ?: return@launch
            c.quranPlayer.play(folder, surahNumber, p.ayah.coerceAtLeast(1), s.ayahCount, repeatCount.value, speed = settings.value.playbackSpeed)
        }
    }

    // ---------- Настройки отображения ----------
    fun setShowTajik(v: Boolean) = viewModelScope.launch { c.settingsRepository.setShowTajik(v) }
    fun setShowEnglish(v: Boolean) = viewModelScope.launch { c.settingsRepository.setShowEnglish(v) }
    fun setShowArabic(v: Boolean) = viewModelScope.launch { c.settingsRepository.setShowArabic(v) }
    fun setShowTranslation(v: Boolean) = viewModelScope.launch { c.settingsRepository.setShowTranslation(v) }
    fun setShowTranslit(v: Boolean) = viewModelScope.launch { c.settingsRepository.setShowTransliteration(v) }
    fun setFontSize(v: Float) = viewModelScope.launch { c.settingsRepository.setArabicFontSize(v) }

    // ---------- Избранное, заметки ----------
    fun toggleFavoriteAyah(ayah: Int) = viewModelScope.launch {
        val dao = c.database.favorites()
        if (ayah in favoriteAyahs.value) dao.removeAyah(surahNumber, ayah) else dao.addAyah(FavoriteAyah(surahNumber, ayah))
    }

    fun toggleFavoriteSurah() = viewModelScope.launch {
        val dao = c.database.favorites()
        if (isFavoriteSurah.value) dao.removeSurah(surahNumber) else dao.addSurah(FavoriteSurah(surahNumber))
    }

    fun saveNote(ayah: Int, text: String) = viewModelScope.launch {
        if (text.isBlank()) c.database.notes().delete(surahNumber, ayah)
        else c.database.notes().save(AyahNote(surahNumber, ayah, text.trim()))
    }

    fun copy(context: Context, ayah: Ayah) {
        val s = surah.value
        val text = buildString {
            appendLine(ayah.arabic)
            appendLine(ayah.translation)
            append("(${s?.russianName ?: "Сура ${ayah.surah}"}, аят ${ayah.number})")
        }
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Аят", text))
    }

    fun saveLastRead(ayah: Int) = viewModelScope.launch {
        if (ayah >= 1) c.settingsRepository.setLastRead(surahNumber, ayah)
    }
}
