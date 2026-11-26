package com.alif.app.data.quran

data class Surah(
    val number: Int,
    val arabicName: String,
    val russianName: String,
    val meaning: String,
    val ayahCount: Int,
    val meccan: Boolean,
) {
    /** Басмала перед сурой: есть у всех, кроме 1-й (там это первый аят) и 9-й. */
    val hasBismillahHeader: Boolean get() = number != 1 && number != 9
}

data class Ayah(
    val surah: Int,
    val number: Int,
    val arabic: String,
    val transliteration: String,
    val translation: String,
)

/** Чтец с everyayah.com. folder — имя папки в URL. */
data class Reciter(val folder: String, val name: String, val note: String = "")

object Reciters {
    val all = listOf(
        Reciter("Alafasy_128kbps", "Мишари Аль-Афаси", "Кувейт · мягкий, мелодичный голос"),
        Reciter("Abdurrahmaan_As-Sudais_192kbps", "Абдуррахман Ас-Судайс", "имам Мекки · Аль-Харам"),
        Reciter("MaherAlMuaiqly128kbps", "Махер Аль-Муайкли", "имам Мекки · спокойное чтение"),
        Reciter("Abdul_Basit_Murattal_192kbps", "Абдуль-Басит Абдус-Самад", "Египет · классика"),
        Reciter("Yasser_Ad-Dussary_128kbps", "Ясир Ад-Даусари", "имам Мекки · выразительное чтение"),
    )

    fun byFolder(folder: String): Reciter = all.firstOrNull { it.folder == folder } ?: all.first()

    /** Ссылка на аудио одного аята: https://everyayah.com/data/<папка>/SSSAAA.mp3 */
    fun ayahUrl(folder: String, surah: Int, ayah: Int): String =
        "https://everyayah.com/data/$folder/%03d%03d.mp3".format(surah, ayah)
}
