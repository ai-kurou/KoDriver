package kurou.kodriver.core.narrator

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import kotlin.coroutines.resume

/**
 * WAV 音声を再生する Android 向け実装。
 */
class AndroidSoundPlayer internal constructor(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
    private val captureError: (Throwable) -> Unit,
    private val usageProvider: suspend () -> Int,
    private val soundPoolFactory: (usage: Int) -> SoundPool,
    initialUsage: Int = AudioAttributes.USAGE_MEDIA,
) : SoundPlayer {
    private var soundPool = soundPoolFactory(initialUsage)
    private var soundPoolUsage = initialUsage

    private val loadLock = Any()
    private var currentStreamId: Int = 0

    // 前の音声をアンロードする前に次の音声をロードすることで、Bluetooth A2DP 接続のアイドル化を防ぐ
    private var loadedSoundId: Int = 0

    constructor(context: Context, usageProvider: suspend () -> Int = { AudioAttributes.USAGE_MEDIA }) : this(
        context,
        Dispatchers.IO,
        ::captureNarratorError,
        usageProvider,
        ::createSoundPool,
    )

    override val isPlaying: Boolean
        get() = currentStreamId != 0

    override suspend fun play(
        bytes: ByteArray,
        volume: Int,
    ) {
        var temp: File? = null
        try {
            val usage =
                try {
                    usageProvider()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    AudioAttributes.USAGE_MEDIA
                }
            synchronized(loadLock) {
                if (usage != soundPoolUsage) {
                    soundPool.release()
                    soundPool = soundPoolFactory(usage)
                    soundPoolUsage = usage
                    loadedSoundId = 0
                    currentStreamId = 0
                }
            }
            val playbackFile =
                withContext(ioDispatcher) {
                    File.createTempFile("snd_", ".wav", context.cacheDir).also { it.writeBytes(bytes) }
                }
            temp = playbackFile
            val durationMs = playbackDurationMs(bytes)
            val pool = synchronized(loadLock) { soundPool }
            val soundId = loadSound(pool, playbackFile.absolutePath)
            if (soundId == 0) return
            val v = (volume / 100.0f).coerceIn(0f, 1f)
            val streamId = pool.play(soundId, v, v, 1, 0, 1.0f)
            if (streamId == 0) {
                pool.unload(soundId)
                captureError(IllegalStateException("SoundPool.play() failed"))
                return
            }
            synchronized(loadLock) { currentStreamId = streamId }
            // 新しい音声の再生開始後に前の音声をアンロードする（セッションを維持）
            val prevSoundId =
                synchronized(loadLock) {
                    loadedSoundId.also { loadedSoundId = soundId }
                }
            if (prevSoundId != 0) pool.unload(prevSoundId)
            try {
                delay(durationMs)
            } finally {
                pool.stop(streamId)
                synchronized(loadLock) { currentStreamId = 0 }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            captureError(e)
        } finally {
            temp?.delete()
        }
    }

    // onLoadComplete が万一発火しなかった場合でも playJob を永久にサスペンドさせず、
    // 以後の読み上げ（優先度判定）を止めないための保険。
    private suspend fun loadSound(
        pool: SoundPool,
        path: String,
    ): Int {
        val soundId = withTimeoutOrNull(LOAD_TIMEOUT_MS) { awaitLoad(pool, path) }
        if (soundId == null) {
            captureError(IllegalStateException("SoundPool load timed out: $path"))
            return 0
        }
        return soundId
    }

    private suspend fun awaitLoad(
        pool: SoundPool,
        path: String,
    ): Int =
        suspendCancellableCoroutine { cont ->
            // load() より先にリスナーを登録する。逆順だと、小さい WAV のロードが
            // リスナー登録前に完了して onLoadComplete が捨てられ、永久にサスペンドする。
            // リスナーは別スレッドから soundId 代入前に発火しうるため loadLock で待ち合わせる。
            var soundId = 0
            pool.setOnLoadCompleteListener { _, loadedId, status ->
                val expectedId = synchronized(loadLock) { soundId }
                if (loadedId == expectedId) {
                    pool.setOnLoadCompleteListener(null)
                    if (cont.isActive) {
                        if (status == 0) {
                            cont.resume(loadedId)
                        } else {
                            captureError(IllegalStateException("SoundPool load failed: status=$status"))
                            pool.unload(loadedId)
                            cont.resume(0)
                        }
                    }
                }
            }
            synchronized(loadLock) { soundId = pool.load(path, 1) }
            cont.invokeOnCancellation {
                synchronized(loadLock) { soundId }.takeIf { it != 0 }?.let { pool.unload(it) }
            }
        }

    /**
     * [bytes] の再生時間。`SoundPool` は再生完了を通知しないため、この時間だけ待ってから停止する。
     *
     * ヘッダから再生時間を判定できない場合は、[FALLBACK_DURATION_MS] で代替する。0 を返して即座に
     * `stop()` すると音声が全く鳴らなくなるため、同梱 WAV の最長（約 5.7 秒）を上回る時間を待つ。
     * 優先度の高いイベントで割り込む際は再生ジョブ自体がキャンセルされるため、待ち時間が長めでも
     * 割り込みの応答性には影響しない。
     */
    private fun playbackDurationMs(bytes: ByteArray): Long =
        wavDurationMs(bytes) ?: run {
            captureError(IllegalStateException("WAV duration unknown: ${bytes.size} bytes"))
            FALLBACK_DURATION_MS
        }

    private companion object {
        const val LOAD_TIMEOUT_MS = 5_000L
        const val FALLBACK_DURATION_MS = 6_000L
    }
}

private fun createSoundPool(usage: Int): SoundPool =
    SoundPool
        .Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes
                .Builder()
                .setUsage(usage)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        ).build()
