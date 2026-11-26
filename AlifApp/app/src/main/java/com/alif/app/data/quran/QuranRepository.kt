package com.alif.app.data.quran

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Текст Корана хранится в assets/quran/quran.tsv (Tanzil Uthmani + перевод Кулиева)
 * и целиком загружается в память при первом обращении (~4 МБ, около секунды).
 */
class QuranRepository(private val context: Context) {

    private val mutex = Mutex()
    private var surahs: List<Surah>? = null
    private var ayahs: Array<List<Ayah>>? = null

    suspend fun surahs(): List<Surah> = mutex.withLock {
        surahs ?: withContext(Dispatchers.IO) { loadSurahs() }.also { surahs = it }
    }

    suspend fun surah(number: Int): Surah = surahs()[number - 1]

    suspend fun ayahs(surah: Int): List<Ayah> = allAyahs()[surah - 1]

    suspend fun ayah(surah: Int, ayah: Int): Ayah? = ayahs(surah).getOrNull(ayah - 1)

    private suspend fun allAyahs(): Array<List<Ayah>> = mutex.withLock {
        ayahs ?: withContext(Dispatchers.IO) { loadAyahs() }.also { ayahs = it }
    }

    private fun loadSurahs(): List<Surah> =
        context.assets.open("quran/surahs.tsv").bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.filter { it.isNotBlank() && !it.startsWith("#") }.map { line ->
                val c = line.split('\t')
                Surah(
                    number = c[0].toInt(),
                    arabicName = c[1],
                    russianName = c[2],
                    meaning = c[3],
                    ayahCount = c[4].toInt(),
                    meccan = c[5] == "m",
                )
            }.toList()
        }

    private fun loadAyahs(): Array<List<Ayah>> {
        val buckets = Array(114) { ArrayList<Ayah>() }
        context.assets.open("quran/quran.tsv").bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.forEach { line ->
                if (line.isEmpty() || line[0] == '#') return@forEach
                val c = line.split('\t')
                val s = c[0].toInt()
                val a = c[1].toInt()
                buckets[s - 1].add(Ayah(s, a, displayText(s, a, c[2]), c[3], c[4]))
            }
        }
        return Array(114) { buckets[it] }
    }

    /**
     * В тексте Tanzil басмала входит в начало 1-го аята каждой суры (кроме 1-й и 9-й).
     * Сам файл не меняем (условие лицензии), а при показе убираем её — она выводится отдельной шапкой суры.
     */
    private fun displayText(surah: Int, ayah: Int, text: String): String {
        if (ayah != 1 || surah == 1 || surah == 9) return text
        val words = text.split(' ')
        if (words.size <= 4) return text
        val first4 = words.take(4).joinToString(" ").replace(SHADDA, "")
        return if (first4 == BISMILLAH_NO_SHADDA) words.drop(4).joinToString(" ") else text
    }

    private companion object {
        const val SHADDA = "\u0651"
        val BISMILLAH_NO_SHADDA = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ".replace(SHADDA, "")
    }

    /** Поиск сур по номеру, русскому или арабскому названию. */
    suspend fun search(query: String): List<Surah> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return surahs()
        return surahs().filter { s ->
            s.number.toString() == q ||
                s.russianName.lowercase().contains(q) ||
                s.meaning.lowercase().contains(q) ||
                s.arabicName.contains(q) ||
                s.russianName.lowercase().replace("-", " ").contains(q)
        }
    }
}
