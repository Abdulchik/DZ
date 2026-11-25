package com.alif.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector
import com.alif.app.R

object Routes {
    const val LEARN = "learn"
    const val QURAN = "quran"
    const val FAVORITES = "favorites"
    const val PROFILE = "profile"
    const val ABOUT = "about"
    const val LESSON = "lesson/{id}"
    const val READER = "reader/{surah}/{ayah}"

    fun lesson(id: String) = "lesson/$id"
    fun reader(surah: Int, ayah: Int) = "reader/$surah/$ayah"
}

/** Четыре вкладки нижней навигации. */
enum class TopLevelTab(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    LEARN(Routes.LEARN, R.string.tab_learn, Icons.Outlined.School, Icons.Filled.School),
    QURAN(Routes.QURAN, R.string.tab_quran, Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Filled.MenuBook),
    FAVORITES(Routes.FAVORITES, R.string.tab_favorites, Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
    PROFILE(Routes.PROFILE, R.string.tab_profile, Icons.Outlined.Person, Icons.Filled.Person),
}
