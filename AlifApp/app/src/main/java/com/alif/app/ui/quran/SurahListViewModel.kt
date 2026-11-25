package com.alif.app.ui.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alif.app.AppContainer
import com.alif.app.data.db.FavoriteSurah
import com.alif.app.data.quran.Surah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SurahListState(
    val surahs: List<Surah> = emptyList(),
    val favorites: Set<Int> = emptySet(),
    val lastSurah: Surah? = null,
    val lastAyah: Int = 0,
    val loading: Boolean = true,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SurahListViewModel(private val c: AppContainer) : ViewModel() {
    val query = MutableStateFlow("")

    private val results = query.mapLatest { c.quranRepository.search(it) }

    val state: StateFlow<SurahListState> = combine(
        results,
        c.database.favorites().surahs().map { l -> l.map { it.surah }.toSet() },
        c.settingsRepository.settings,
    ) { list, fav, settings ->
        val last = if (settings.lastSurah in 1..114) c.quranRepository.surah(settings.lastSurah) else null
        SurahListState(list, fav, last, settings.lastAyah, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SurahListState())

    fun toggleFavorite(surah: Int) = viewModelScope.launch {
        val dao = c.database.favorites()
        if (surah in state.value.favorites) dao.removeSurah(surah) else dao.addSurah(FavoriteSurah(surah))
    }
}
