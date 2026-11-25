package com.alif.app.data.audio

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.alif.app.data.quran.Reciters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

/**
 * Аудио Корана по аятам с everyayah.com.
 * Скачанные аяты сохраняются в кэш (до 1 ГБ) и потом играют без интернета.
 */
@OptIn(UnstableApi::class)
class QuranPlayer(context: Context) {

    data class State(
        val surah: Int = 0,
        /** Текущий аят; 0 — басмала перед сурой. */
        val ayah: Int = -1,
        val isPlaying: Boolean = false,
        val isBuffering: Boolean = false,
        val active: Boolean = false,
        val repeatCount: Int = 1,
        val error: String? = null,
    )

    private val cache = SimpleCache(
        File(context.filesDir, "quran_audio"),
        LeastRecentlyUsedCacheEvictor(1024L * 1024 * 1024),
        StandaloneDatabaseProvider(context),
    )

    private val dataSourceFactory = CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(
            DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setUserAgent("AlifQuranApp")
        )
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
        .setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(),
            true,
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val (s, a) = parseId(mediaItem?.mediaId) ?: return
                _state.update { it.copy(surah = s, ayah = a, error = null) }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.update {
                    it.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                        active = playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED,
                        ayah = if (playbackState == Player.STATE_ENDED) -1 else it.ayah,
                    )
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.update {
                    it.copy(
                        isPlaying = false, isBuffering = false,
                        error = "Не удалось загрузить аудио. Проверьте интернет (скачанные аяты играют и без него).",
                    )
                }
            }
        })
    }

    private fun item(reciter: String, surah: Int, ayah: Int, rep: Int): MediaItem {
        // Басмала (ayah = 0) — это первый аят суры «Аль-Фатиха»
        val url = if (ayah == 0) Reciters.ayahUrl(reciter, 1, 1) else Reciters.ayahUrl(reciter, surah, ayah)
        return MediaItem.Builder().setUri(url).setMediaId("$surah:$ayah:$rep").build()
    }

    private fun parseId(id: String?): Pair<Int, Int>? {
        val p = id?.split(':') ?: return null
        if (p.size < 2) return null
        return p[0].toIntOrNull()?.let { s -> p[1].toIntOrNull()?.let { a -> s to a } }
    }

    /**
     * Играть суру с аята [fromAyah] до [toAyah]; каждый аят повторяется [repeat] раз.
     * Если [toAyah] == [fromAyah] — режим «повторять один аят».
     */
    fun play(
        reciter: String,
        surah: Int,
        fromAyah: Int,
        toAyah: Int,
        repeat: Int = 1,
        withBismillah: Boolean = false,
        speed: Float = 1f,
    ) {
        val items = buildList {
            if (withBismillah) add(item(reciter, surah, 0, 0))
            for (a in fromAyah..toAyah) repeat(repeat.coerceAtLeast(1)) { r -> add(item(reciter, surah, a, r)) }
        }
        _state.update { State(surah = surah, ayah = if (withBismillah) 0 else fromAyah, repeatCount = repeat, active = true) }
        player.setMediaItems(items)
        player.setPlaybackSpeed(speed)
        player.prepare()
        player.play()
    }

    fun togglePause() {
        if (player.isPlaying) player.pause() else {
            if (player.playbackState == Player.STATE_IDLE) player.prepare()
            player.play()
        }
    }

    fun setSpeed(speed: Float) = player.setPlaybackSpeed(speed)

    fun stop() {
        player.stop()
        player.clearMediaItems()
        _state.update { State() }
    }

    /** Перейти к следующему/предыдущему аяту (пропуская повторы). */
    fun skip(forward: Boolean) {
        val current = _state.value.ayah
        val count = player.mediaItemCount
        val range = if (forward) (player.currentMediaItemIndex + 1 until count) else (player.currentMediaItemIndex - 1 downTo 0)
        for (i in range) {
            val (_, a) = parseId(player.getMediaItemAt(i).mediaId) ?: continue
            if (a != current) {
                // назад — к первому повтору предыдущего аята
                var target = i
                if (!forward) {
                    while (target > 0 && parseId(player.getMediaItemAt(target - 1).mediaId)?.second == a) target--
                }
                player.seekTo(target, 0)
                return
            }
        }
    }
}
