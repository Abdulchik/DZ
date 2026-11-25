package com.alif.app.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.app.R
import com.alif.app.ui.LocalSnackbar
import com.alif.app.ui.appViewModelFactory
import com.alif.app.ui.components.ArabicText
import com.alif.app.ui.components.EmptyState
import com.alif.app.ui.components.ScreenHeader
import com.alif.app.ui.components.rememberHaptics
import com.alif.app.ui.quran.NoteDialog
import com.alif.app.ui.quran.SurahRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onOpenAyah: (surah: Int, ayah: Int) -> Unit,
    vm: FavoritesViewModel = viewModel(factory = appViewModelFactory { FavoritesViewModel(it) }),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val sort by vm.sort.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var sortMenu by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AyahEntry?>(null) }
    val haptics = rememberHaptics()
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()

    val tabs = listOf(
        stringResource(R.string.favorites_ayahs) + " (${state.ayahs.size})",
        stringResource(R.string.favorites_surahs) + " (${state.surahs.size})",
        stringResource(R.string.favorites_lessons) + " (${state.items.size})",
        "Заметки (${state.notesOnly.size})",
    )

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.favorites_title))
        Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                placeholder = { Text("Поиск") },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
            )
            Box {
                IconButton(onClick = { sortMenu = true }) { Icon(Icons.AutoMirrored.Filled.Sort, "Сортировка") }
                DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                    SortMode.entries.forEach { m ->
                        DropdownMenuItem(
                            text = { Text((if (m == sort) "✓ " else "") + m.title) },
                            onClick = { vm.sort.value = m; sortMenu = false },
                        )
                    }
                }
            }
        }
        PrimaryScrollableTabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.background, edgePadding = 16.dp) {
            tabs.forEachIndexed { i, title ->
                Tab(selected = tab == i, onClick = { haptics.tap(); tab = i }, text = { Text(title) })
            }
        }

        val empty = when (tab) {
            0 -> state.ayahs.isEmpty(); 1 -> state.surahs.isEmpty(); 2 -> state.items.isEmpty(); else -> state.notesOnly.isEmpty()
        }
        if (empty) {
            EmptyState(
                Icons.Outlined.FavoriteBorder,
                if (tab == 3) "Заметок пока нет. Нажмите на аят в Коране → «Добавить заметку»." else stringResource(R.string.favorites_empty),
            )
            return@Column
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (tab) {
                0, 3 -> items(if (tab == 0) state.ayahs else state.notesOnly, key = { "${it.ayah.surah}:${it.ayah.number}" }) { e ->
                    AyahCard(
                        e,
                        onOpen = { onOpenAyah(e.ayah.surah, e.ayah.number) },
                        onEdit = { editing = e },
                        onRemove = if (tab == 0) ({ vm.removeAyah(e); Unit }) else null,
                    )
                }
                1 -> items(state.surahs, key = { it.surah.number }) { e ->
                    Box(Modifier.padding(horizontal = 0.dp)) {
                        SurahRow(e.surah, true, onClick = { onOpenAyah(e.surah.number, 0) }, onFavorite = { vm.removeSurah(e.surah.number) })
                    }
                }
                else -> items(state.items, key = { it.key }) { item ->
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLowest, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            ArabicText(item.arabic, fontSize = 40.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                                Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                if (!vm.playItem(item.key)) scope.launch { snackbar.showSnackbar("Аудио ещё не добавлено") }
                            }) { Icon(Icons.AutoMirrored.Filled.VolumeUp, "Прослушать") }
                            IconButton(onClick = { vm.removeItem(item.key) }) { Icon(Icons.Filled.Delete, "Удалить") }
                        }
                    }
                }
            }
        }
    }

    editing?.let { e ->
        NoteDialog(
            initial = e.note.orEmpty(),
            title = "Заметка: ${e.surah.russianName}, ${e.ayah.number}",
            onDismiss = { editing = null },
            onSave = { vm.saveNote(e, it); editing = null },
        )
    }
}

@Composable
private fun AyahCard(e: AyahEntry, onOpen: () -> Unit, onEdit: () -> Unit, onRemove: (() -> Unit)?) {
    val colors = MaterialTheme.colorScheme
    Surface(onClick = onOpen, shape = MaterialTheme.shapes.large, color = colors.surfaceContainerLowest, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${e.surah.russianName} · аят ${e.ayah.number}", style = MaterialTheme.typography.titleSmall, color = colors.secondary, modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "Заметка") }
                if (onRemove != null) IconButton(onClick = onRemove) { Icon(Icons.Filled.Delete, "Убрать") }
            }
            ArabicText(e.ayah.arabic, quran = true, modifier = Modifier.fillMaxWidth())
            Text(e.ayah.translation, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            e.note?.let {
                Surface(shape = MaterialTheme.shapes.medium, color = colors.surfaceContainer) {
                    Row(Modifier.padding(10.dp)) {
                        Icon(Icons.AutoMirrored.Filled.Notes, null, tint = colors.secondary)
                        Spacer(Modifier.width(8.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
