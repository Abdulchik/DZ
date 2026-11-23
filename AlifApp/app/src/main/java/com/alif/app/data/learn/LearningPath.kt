package com.alif.app.data.learn

import androidx.annotation.StringRes
import com.alif.app.R

/** Модуль на пути обучения. Сами уроки внутри модулей добавляются на этапах 2–4. */
data class LearningModule(
    val id: String,
    val number: Int,
    @StringRes val title: Int,
    @StringRes val description: Int,
    /** Арабский символ-обложка в кружке на пути. */
    val symbol: String,
    val lessonCount: Int,
)

object LearningPath {
    val modules: List<LearningModule> = listOf(
        LearningModule("alphabet", 1, R.string.module_alphabet, R.string.module_alphabet_desc, "ا ب ت", 8),
        LearningModule("forms", 2, R.string.module_forms, R.string.module_forms_desc, "ـبـ", 4),
        LearningModule("joining", 3, R.string.module_joining, R.string.module_joining_desc, "كتب", 4),
        LearningModule("harakat", 4, R.string.module_harakat, R.string.module_harakat_desc, "بَ بِ بُ", 6),
        LearningModule("madd", 5, R.string.module_madd, R.string.module_madd_desc, "بَا", 3),
        LearningModule("special", 6, R.string.module_special, R.string.module_special_desc, "ة ء لا", 5),
        LearningModule("reading", 7, R.string.module_reading, R.string.module_reading_desc, "كِتَاب", 6),
        LearningModule("tajweed", 8, R.string.module_tajweed, R.string.module_tajweed_desc, "ۚ ۖ", 5),
    )
}
