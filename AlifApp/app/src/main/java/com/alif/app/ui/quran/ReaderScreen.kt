package com.alif.app.ui.quran

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.app.data.quran.Ayah
import com.alif.app.data.quran.Reciters
import com.alif.app.data.settings.AppSettings
import com.alif.app.ui.LocalSnackbar
import com.alif.app.ui.appViewModelFactory
import com.alif.app.ui.components.ArabicText
import com.alif.app.ui.components.rememberHaptics
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private val SPEEDS = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f)
private val REPEATS = listOf(1, 3, 5, 10, 20)

@OptIn(ExperimentalMaterial3Api::class, kotlinx.coroutines.FlowPreview::class)
@Composable
fun ReaderScreen(surahNumber: Int, startAyah: Int, onBack: () -> Unit) {
    val vm: ReaderViewModel = viewModel(
        key = "reader_$surahNumber",
        factory = appViewModelFactory { ReaderViewModel(it, surahNumber) },
    )
    val surah by vm.surah.collectAsStateWithLifecycle()
    val ayahs by vm.ayahs.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val favorites by vm.favoriteAyahs.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()
    val isFavSurah by vm.isFavoriteSurah.collectAsStateWithLifecycle()
    val player by vm.player.collectAsStateWithLifecycle()
    val repeat by vm.repeatCount.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()

    // Список: 0 — шапка суры, дальше аяты
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = if (startAyah > 1) startAyah else 0)
    var menuAyah by remember { mutableStateOf<Ayah?>(null) }
    var noteAyah by remember { mutableStateOf<Ayah?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showReciters by rememberSaveable { mutableStateOf(false) }

    val playingHere = player.active && player.surah == surahNumber

    // Запоминаем, где остановились
    LaunchedEffect(listState, ayahs) {
        if (ayahs.isEmpty()) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .debounce(800)
            .collect { idx -> vm.saveLastRead(idx.coerceIn(1, ayahs.size)) }
    }
    // Во время чтения чтецом прокручиваем к текущему аяту
    LaunchedEffect(player.ayah, playingHere) {
        if (playingHere && player.ayah >= 1) {
            val visible = listState.layoutInfo.visibleItemsInfo.map { it.index }
            if (player.ayah !in visible.dropLast(1)) listState.animateScrollToItem(player.ayah)
        }
    }
    LaunchedEffect(player.error) {
        player.error?.let { snackbar.showSnackbar(it) }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(surah?.russianName ?: "", style = MaterialTheme.typography.titleLarge)
                    surah?.let {
                        Text("${it.meaning} · ${it.ayahCount} аят.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") } },
            actions = {
                IconButton(onClick = { haptics.tap(); vm.toggleFavoriteSurah() }) {
                    Icon(
                        if (isFavSurah) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, "Сура в избранное",
                        tint = if (isFavSurah) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { showSettings = true }) { Icon(Icons.Filled.Tune, "Настройки чтения") }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            windowInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal),
        )

        if (ayahs.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(key = "header") {
                    surah?.let { s ->
                        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                ArabicText("سُورَةُ " + s.arabicName, quran = true, fontSize = 30.sp, color = MaterialTheme.colorScheme.onPrimary)
                                Text(
                                    "${if (s.meccan) "Мекканская" else "Мединская"} · ${s.ayahCount} аятов",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                if (s.hasBismillahHeader) {
                                    Spacer(Modifier.height(8.dp))
                                    ArabicText("بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", quran = true, color = MaterialTheme.colorScheme.onPrimary, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
                itemsIndexed(ayahs, key = { _, a -> a.number }) { _, ayah ->
                    AyahItem(
                        ayah = ayah,
                        settings = settings,
                        isPlaying = playingHere && player.ayah == ayah.number,
                        isFavorite = ayah.number in favorites,
                        note = notes[ayah.number],
                        onClick = { haptics.tap(); menuAyah = ayah },
                    )
                }
            }
        }

        AudioBar(
            active = playingHere,
            isPlaying = player.isPlaying,
            isBuffering = player.isBuffering,
            currentAyah = player.ayah,
            total = surah?.ayahCount ?: 0,
            speed = settings.playbackSpeed,
            repeat = repeat,
            onPlayPause = { haptics.tap(); vm.togglePause() },
            onStop = vm::stop,
            onSkip = vm::skip,
            onSpeed = { vm.setSpeed(it) },
            onRepeat = vm::setRepeat,
            onStart = { haptics.tap(); vm.playFrom((listState.firstVisibleItemIndex).coerceIn(1, ayahs.size.coerceAtLeast(1))) },
            reciterName = Reciters.byFolder(settings.defaultReciter).name,
            onChooseReciter = { showReciters = true },
        )
    }

    menuAyah?.let { ayah ->
        AyahMenu(
            ayah = ayah,
            isFavorite = ayah.number in favorites,
            hasNote = notes.containsKey(ayah.number),
            onDismiss = { menuAyah = null },
            onPlayFrom = { vm.playFrom(ayah.number); menuAyah = null },
            onRepeat = { n -> vm.repeatAyah(ayah.number, n); menuAyah = null },
            onFavorite = { vm.toggleFavoriteAyah(ayah.number); menuAyah = null },
            onNote = { noteAyah = ayah; menuAyah = null },
            onCopy = {
                vm.copy(context, ayah); menuAyah = null
                scope.launch { snackbar.showSnackbar("Аят скопирован") }
            },
        )
    }

    noteAyah?.let { ayah ->
        NoteDialog(
            initial = notes[ayah.number].orEmpty(),
            title = "Заметка к аяту ${ayah.number}",
            onDismiss = { noteAyah = null },
            onSave = { vm.saveNote(ayah.number, it); noteAyah = null },
        )
    }

    if (showReciters) {
        ReciterDialog(
            selected = Reciters.byFolder(settings.defaultReciter).folder,
            onSelect = { vm.setReciter(it); showReciters = false },
            onDismiss = { showReciters = false },
        )
    }

    if (showSettings) {
        ReaderSettingsSheet(
            settings = settings,
            onDismiss = { showSettings = false },
            onArabic = { vm.setShowArabic(it) },
            onTranslation = { vm.setShowTranslation(it) },
            onTranslit = { vm.setShowTranslit(it) },
            onFontSize = { vm.setFontSize(it) },
            onReciter = { vm.setReciter(it) },
        )
    }
}

@Composable
private fun AyahItem(
    ayah: Ayah,
    settings: AppSettings,
    isPlaying: Boolean,
    isFavorite: Boolean,
    note: String?,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (isPlaying) colors.secondaryContainer else colors.surfaceContainerLowest, label = "ayahBg")
    Surface(onClick = onClick, shape = MaterialTheme.shapes.large, color = bg, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AyahNumberBadge(ayah.number, Modifier.size(38.dp))
                Spacer(Modifier.weight(1f))
                if (note != null) Icon(Icons.AutoMirrored.Filled.Notes, "Есть заметка", tint = colors.secondary, modifier = Modifier.size(20.dp))
                if (isFavorite) Icon(Icons.Filled.Favorite, "В избранном", tint = colors.secondary, modifier = Modifier.padding(start = 8.dp).size(20.dp))
            }
            if (settings.showArabic) {
                ArabicText(
                    ayah.arabic,
                    quran = true,
                    fontSize = (settings.arabicFontSize * 0.9f).sp,
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.onSurface,
                )
            }
            if (settings.showTransliteration) {
                // Чтение кириллицей — крупнее, если арабский скрыт
                Text(
                    ayah.transliteration,
                    style = if (settings.showArabic) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    color = colors.primary,
                )
            }
            if (settings.showTranslation) {
                Text(ayah.translation, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            }
            if (note != null) {
                Surface(shape = MaterialTheme.shapes.medium, color = colors.surfaceContainer) {
                    Text(note, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(10.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AudioBar(
    active: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentAyah: Int,
    total: Int,
    speed: Float,
    repeat: Int,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSkip: (Boolean) -> Unit,
    onSpeed: (Float) -> Unit,
    onRepeat: (Int) -> Unit,
    onStart: () -> Unit,
    reciterName: String,
    onChooseReciter: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surfaceContainerLow, tonalElevation = 3.dp, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            ReciterChip(reciterName, onChooseReciter)
            if (!active) {
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Filled.Headphones, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Слушать суру")
                }
                return@Column
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        when {
                            currentAyah == 0 -> "Басмала"
                            currentAyah > 0 -> "Аят $currentAyah из $total"
                            else -> "Готово"
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (isBuffering) Text("Загрузка…", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                IconButton(onClick = { onSkip(false) }) { Icon(Icons.Filled.SkipPrevious, "Предыдущий аят") }
                FilledIconButton(onClick = onPlayPause, modifier = Modifier.size(52.dp)) {
                    if (isBuffering && !isPlaying) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = colors.onPrimary)
                    else Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Пауза/воспроизведение")
                }
                IconButton(onClick = { onSkip(true) }) { Icon(Icons.Filled.SkipNext, "Следующий аят") }
                IconButton(onClick = onStop) { Icon(Icons.Filled.Stop, "Стоп") }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SPEEDS.forEach { s ->
                    FilterChip(selected = speed == s, onClick = { onSpeed(s) }, label = { Text("${s}x".replace(".0x", "x")) })
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.Center) {
                Icon(Icons.Filled.Repeat, null, tint = colors.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterVertically))
                REPEATS.forEach { n ->
                    FilterChip(selected = repeat == n, onClick = { onRepeat(n) }, label = { Text(if (n == 1) "1 раз" else "×$n") })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AyahMenu(
    ayah: Ayah,
    isFavorite: Boolean,
    hasNote: Boolean,
    onDismiss: () -> Unit,
    onPlayFrom: () -> Unit,
    onRepeat: (Int) -> Unit,
    onFavorite: () -> Unit,
    onNote: () -> Unit,
    onCopy: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 24.dp)) {
            Text("Аят ${ayah.number}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 24.dp))
            val itemColors = ListItemDefaults.colors(containerColor = Color.Transparent)
            ListItem(
                headlineContent = { Text("Слушать с этого аята") },
                leadingContent = { Icon(Icons.Filled.PlayArrow, null) },
                modifier = Modifier.clickableRow(onPlayFrom), colors = itemColors,
            )
            ListItem(
                headlineContent = { Text("Повторять аят (для заучивания)") },
                leadingContent = { Icon(Icons.Filled.Repeat, null) },
                supportingContent = {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(3, 5, 10, 20, 50).forEach { n ->
                            FilterChip(selected = false, onClick = { onRepeat(n) }, label = { Text("×$n") })
                        }
                    }
                },
                colors = itemColors,
            )
            ListItem(
                headlineContent = { Text(if (isFavorite) "Убрать из избранного" else "В избранное") },
                leadingContent = { Icon(if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null) },
                modifier = Modifier.clickableRow(onFavorite), colors = itemColors,
            )
            ListItem(
                headlineContent = { Text(if (hasNote) "Изменить заметку" else "Добавить заметку") },
                leadingContent = { Icon(Icons.Filled.Edit, null) },
                modifier = Modifier.clickableRow(onNote), colors = itemColors,
            )
            ListItem(
                headlineContent = { Text("Копировать") },
                leadingContent = { Icon(Icons.Filled.ContentCopy, null) },
                modifier = Modifier.clickableRow(onCopy), colors = itemColors,
            )
        }
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit) = this.clickable(onClick = onClick)

@Composable
fun NoteDialog(initial: String, title: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().height(160.dp),
                placeholder = { Text("Ваши мысли, перевод слов, что запомнить…") },
            )
        },
        confirmButton = { Button(onClick = { onSave(text) }) { Text("Сохранить") } },
        dismissButton = {
            Row {
                if (initial.isNotEmpty()) TextButton(onClick = { onSave("") }) { Text("Удалить") }
                TextButton(onClick = onDismiss) { Text("Отмена") }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSettingsSheet(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onArabic: (Boolean) -> Unit,
    onTranslation: (Boolean) -> Unit,
    onTranslit: (Boolean) -> Unit,
    onFontSize: (Float) -> Unit,
    onReciter: (String) -> Unit,
) {
    val enabledCount = listOf(settings.showArabic, settings.showTransliteration, settings.showTranslation).count { it }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Настройки чтения", style = MaterialTheme.typography.titleLarge)
            Text("Что показывать (можно убрать любое, но хотя бы одно остаётся):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ToggleRow("Арабский текст", settings.showArabic, enabledCount, onArabic)
            ToggleRow("Чтение кириллицей", settings.showTransliteration, enabledCount, onTranslit)
            ToggleRow("Перевод на русский (Кулиев)", settings.showTranslation, enabledCount, onTranslation)
            var size by remember(settings.arabicFontSize) { mutableFloatStateOf(settings.arabicFontSize) }
            Text("Размер арабского текста: ${size.toInt()}", style = MaterialTheme.typography.titleMedium)
            ArabicText("ٱلْحَمْدُ لِلَّهِ", quran = true, fontSize = size.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Slider(
                value = size,
                onValueChange = { size = it },
                onValueChangeFinished = { onFontSize(size) },
                valueRange = AppSettings.MIN_ARABIC_SIZE..AppSettings.MAX_ARABIC_SIZE,
            )
            HorizontalDivider()
            Text("Чтец", style = MaterialTheme.typography.titleMedium)
            Reciters.all.forEach { r ->
                Row(
                    Modifier.fillMaxWidth().clickableRow { onReciter(r.folder) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = settings.defaultReciter == r.folder, onClick = { onReciter(r.folder) })
                    Column {
                        Text(r.name, style = MaterialTheme.typography.bodyLarge)
                        if (r.note.isNotEmpty()) Text(r.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Text(
                "Аудио загружается из интернета по аятам и сохраняется в телефоне — повторное прослушивание работает без сети.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, enabledCount: Int, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        // Последний включённый переключатель выключить нельзя
        Switch(checked, onChange, enabled = !(checked && enabledCount == 1))
    }
}

@Composable
private fun ReciterChip(name: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.RecordVoiceOver, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Чтец", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Icon(Icons.Filled.ExpandMore, "Выбрать чтеца", tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
private fun ReciterDialog(selected: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите чтеца") },
        text = {
            Column {
                Reciters.all.forEach { r ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onSelect(r.folder) }.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected == r.folder, onClick = { onSelect(r.folder) })
                        Column {
                            Text(r.name, style = MaterialTheme.typography.titleMedium)
                            Text(r.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
    )
}
