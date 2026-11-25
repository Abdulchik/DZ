package com.alif.app.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alif.app.AppContainer
import com.alif.app.data.db.AyahNote
import com.alif.app.data.db.FavoriteItem
import com.alif.app.data.quran.Ayah
import com.alif.app.data.quran.Surah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortMode(val title: String) { NEWEST("Сначала новые"), OLDEST("Сначала старые"), ORDER("По порядку в Коране") }

data class AyahEntry(val ayah: Ayah, val surah: Surah, val note: String?, val addedAt: Long)
data class SurahEntry(val surah: Surah, val addedAt: Long)

data class FavoritesState(
    val ayahs: List<AyahEntry> = emptyList(),
    val surahs: List<SurahEntry> = emptyList(),
    val items: List<FavoriteItem> = emptyList(),
    val notesOnly: List<AyahEntry> = emptyList(),
)

class FavoritesViewModel(private val c: AppContainer) : ViewModel() {
    val query = MutableStateFlow("")
    val sort = MutableStateFlow(SortMode.NEWEST)

    private val dao = c.database.favorites()

    val state: StateFlow<FavoritesState> = combine(
        dao.ayahs(), dao.surahs(), dao.items(), c.database.notes().all(), combine(query, sort) { q, s -> q to s },
    ) { favAyahs, favSurahs, items, notes, (q, sort) ->
        val repo = c.quranRepository
        val noteMap = notes.associateBy { it.surah to it.ayah }
        suspend fun entry(s: Int, a: Int, added: Long): AyahEntry? {
            val ayah = repo.ayah(s, a) ?: return null
            return AyahEntry(ayah, repo.surah(s), noteMap[s to a]?.text, added)
        }
        val ql = q.trim().lowercase()
        fun AyahEntry.matches() = ql.isEmpty() || ayah.translation.lowercase().contains(ql) ||
            surah.russianName.lowercase().contains(ql) || (note?.lowercase()?.contains(ql) == true) || ayah.arabic.contains(ql)

        fun <T> List<T>.arrange(added: (T) -> Long, order: (T) -> Int): List<T> = when (sort) {
            SortMode.NEWEST -> sortedByDescending(added)
            SortMode.OLDEST -> sortedBy(added)
            SortMode.ORDER -> sortedBy(order)
        }

        val ayahEntries = favAyahs.mapNotNull { entry(it.surah, it.ayah, it.addedAt) }.filter { it.matches() }
            .arrange({ it.addedAt }, { it.ayah.surah * 1000 + it.ayah.number })
        val noteEntries = notes.mapNotNull { n: AyahNote -> entry(n.surah, n.ayah, n.updatedAt) }.filter { it.matches() }
            .arrange({ it.addedAt }, { it.ayah.surah * 1000 + it.ayah.number })
        val surahEntries = favSurahs.map { SurahEntry(repo.surah(it.surah), it.addedAt) }
            .filter { ql.isEmpty() || it.surah.russianName.lowercase().contains(ql) || it.surah.meaning.lowercase().contains(ql) }
            .arrange({ it.addedAt }, { it.surah.number })
        val itemEntries = items.filter { ql.isEmpty() || it.title.lowercase().contains(ql) || it.subtitle.lowercase().contains(ql) || it.arabic.contains(ql) }
            .arrange({ it.addedAt }, { it.key.hashCode() })
        FavoritesState(ayahEntries, surahEntries, itemEntries, noteEntries)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesState())

    fun removeAyah(e: AyahEntry) = viewModelScope.launch { dao.removeAyah(e.ayah.surah, e.ayah.number) }
    fun removeSurah(s: Int) = viewModelScope.launch { dao.removeSurah(s) }
    fun removeItem(key: String) = viewModelScope.launch { dao.removeItem(key) }
    fun saveNote(e: AyahEntry, text: String) = viewModelScope.launch {
        if (text.isBlank()) c.database.notes().delete(e.ayah.surah, e.ayah.number)
        else c.database.notes().save(AyahNote(e.ayah.surah, e.ayah.number, text.trim()))
    }
    fun playItem(key: String): Boolean {
        val audio = when {
            key.startsWith("letter:") -> "letters/" + key.substringAfter(':')
            key.startsWith("word:") -> "words/" + key.substringAfter(':')
            else -> return false
        }
        return c.assetAudio.play(audio)
    }
}
