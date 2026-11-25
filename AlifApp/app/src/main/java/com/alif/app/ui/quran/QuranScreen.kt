package com.alif.app.ui.quran

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.app.R
import com.alif.app.data.quran.Surah
import com.alif.app.ui.appViewModelFactory
import com.alif.app.ui.components.ArabicText
import com.alif.app.ui.components.IconBadge
import com.alif.app.ui.components.ScreenHeader
import com.alif.app.ui.components.SectionCard

@Composable
fun QuranScreen(
    onOpenSurah: (surah: Int, ayah: Int) -> Unit,
    vm: SurahListViewModel = viewModel(factory = appViewModelFactory { SurahListViewModel(it) }),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { ScreenHeader(stringResource(R.string.quran_title)) }
        state.lastSurah?.let { last ->
            item {
                SectionCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp), onClick = { onOpenSurah(last.number, state.lastAyah) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Filled.Bookmark)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.quran_continue_reading), style = MaterialTheme.typography.titleMedium)
                            Text("${last.russianName} · аят ${state.lastAyah}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ArabicText(last.arabicName, quran = true, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = { vm.query.value = "" }) { Icon(Icons.Filled.Clear, "Очистить") }
                },
                placeholder = { Text(stringResource(R.string.quran_search_hint)) },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
            )
        }
        if (state.loading) {
            item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        }
        items(state.surahs, key = { it.number }) { s ->
            SurahRow(s, s.number in state.favorites, onClick = { onOpenSurah(s.number, 0) }, onFavorite = { vm.toggleFavorite(s.number) })
        }
    }
}

@Composable
fun SurahRow(s: Surah, favorite: Boolean, onClick: () -> Unit, onFavorite: (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            AyahNumberBadge(s.number, Modifier.size(44.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(s.russianName, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${s.meaning} · ${s.ayahCount} аят. · ${if (s.meccan) "мекканская" else "мединская"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            ArabicText(s.arabicName, quran = true, fontSize = 22.sp, color = colors.primary)
            if (onFavorite != null) {
                IconButton(onClick = onFavorite) {
                    Icon(
                        if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        "В избранное",
                        tint = if (favorite) colors.secondary else colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
