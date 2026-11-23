package com.alif.app.ui.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alif.app.R
import com.alif.app.ui.components.EmptyState
import com.alif.app.ui.components.ScreenHeader
import com.alif.app.ui.components.rememberHaptics

/** Каркас вкладки «Избранное». Данные и заметки — этап 7. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val haptics = rememberHaptics()
    val tabs = listOf(R.string.favorites_ayahs, R.string.favorites_surahs, R.string.favorites_lessons)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.favorites_title))
        PrimaryTabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.background) {
            tabs.forEachIndexed { i, title ->
                Tab(
                    selected = tab == i,
                    onClick = { haptics.tap(); tab = i },
                    text = { Text(stringResource(title)) },
                )
            }
        }
        EmptyState(Icons.Outlined.FavoriteBorder, stringResource(R.string.favorites_empty))
    }
}
