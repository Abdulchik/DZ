package com.alif.app.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

/**
 * Проигрывает записи букв, слогов и слов из assets/audio.
 * key — путь без расширения, например "letters/ba" или "syllables/ba_fatha".
 * Подходят файлы .mp3, .m4a, .ogg, .wav — можно положить запись прямо с диктофона телефона.
 */
class AssetAudio(private val context: Context) {

    private val extensions = listOf("mp3", "m4a", "ogg", "wav")
    private val folderCache = HashMap<String, Set<String>>()
    private var player: MediaPlayer? = null

    private fun filesIn(folder: String): Set<String> = folderCache.getOrPut(folder) {
        runCatching { context.assets.list("audio/$folder")?.toSet() }.getOrNull() ?: emptySet()
    }

    /** Полное имя файла в assets или null, если записи нет. */
    fun resolve(key: String): String? {
        val folder = key.substringBeforeLast('/', "")
        val name = key.substringAfterLast('/')
        val files = filesIn(folder)
        val ext = extensions.firstOrNull { "$name.$it" in files } ?: return null
        return "audio/$key.$ext"
    }

    fun exists(key: String): Boolean = resolve(key) != null

    /** @return false, если файла нет (тогда покажите подсказку пользователю). */
    fun play(key: String, onDone: (() -> Unit)? = null): Boolean {
        val path = resolve(key) ?: return false
        stop()
        return runCatching {
            context.assets.openFd(path).use { afd ->
                player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    setOnCompletionListener { onDone?.invoke() }
                    prepare()
                    start()
                }
            }
            true
        }.getOrDefault(false)
    }

    fun stop() {
        player?.runCatching { stop(); release() }
        player = null
    }
}

/** Короткие звуки правильного/неправильного ответа (assets/audio/ui). */
class UiSounds(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()

    private val ids = HashMap<String, Int>()

    init {
        val files = runCatching { context.assets.list("audio/ui")?.toList() }.getOrNull().orEmpty()
        for (name in listOf("correct", "wrong", "complete")) {
            val file = files.firstOrNull { it.substringBeforeLast('.') == name } ?: continue
            runCatching { context.assets.openFd("audio/ui/$file").use { ids[name] = pool.load(it, 1) } }
        }
    }

    fun correct() = play("correct")
    fun wrong() = play("wrong")
    fun complete() = play("complete")

    private fun play(name: String) {
        ids[name]?.let { pool.play(it, 0.8f, 0.8f, 1, 0, 1f) }
    }
}
