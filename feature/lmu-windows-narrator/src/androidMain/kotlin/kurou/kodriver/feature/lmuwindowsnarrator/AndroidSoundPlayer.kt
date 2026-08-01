package kurou.kodriver.feature.lmuwindowsnarrator

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CancellationException
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
class AndroidSoundPlayer(
    private val context: Context,
) : SoundPlayer {

    private val soundPool = SoundPool
        .Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        ).build()

    private var currentStreamId: Int = 0

    private val loadLock = Any()

    // 前の音声をアンロードする前に次の音声をロードすることで、
    // Bluetooth A2DP 接続のアイドル化を防ぐ
    private var loadedSoundId: Int = 0

    override val isPlaying: Boolean
        get() = currentStreamId != 0

    override suspend fun play(bytes: ByteArray, volume: Int) {
        val temp = withContext(Dispatchers.IO) {
            File.createTempFile("snd_", ".wav", context.cacheDir).also { it.writeBytes(bytes) }
        }
        try {
            val durationMs = wavDurationMs(bytes)
            val soundId = loadSound(temp.absolutePath)
            if (soundId == 0) return
            val v = (volume / 100.0f).coerceIn(0f, 1f)
            val streamId = soundPool.play(soundId, v, v, 1, 0, 1.0f)
            if (streamId == 0) {
                soundPool.unload(soundId)
                captureNarratorError(IllegalStateException("SoundPool.play() failed"))
                return
            }
            currentStreamId = streamId
            // 新しい音声の再生開始後に前の音声をアンロードする（セッションを維持）
            val prevSoundId = loadedSoundId
            loadedSoundId = soundId
            if (prevSoundId != 0) soundPool.unload(prevSoundId)
            try {
                delay(durationMs)
            } finally {
                soundPool.stop(streamId)
                currentStreamId = 0
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            captureNarratorError(e)
        } finally {
            temp.delete()
        }
    }

    // onLoadComplete が万一発火しなかった場合でも playJob を永久にサスペンドさせず、
    // 以後の読み上げ（優先度判定）を止めないための保険。
    private suspend fun loadSound(path: String): Int {
        val soundId = withTimeoutOrNull(LOAD_TIMEOUT_MS) { awaitLoad(path) }
        if (soundId == null) {
            captureNarratorError(IllegalStateException("SoundPool load timed out: $path"))
            return 0
        }
        return soundId
    }

    private suspend fun awaitLoad(path: String): Int = suspendCancellableCoroutine { cont ->
        // load() より先にリスナーを登録する。逆順だと、小さい WAV のロードが
        // リスナー登録前に完了して onLoadComplete が捨てられ、永久にサスペンドする。
        // リスナーは別スレッドから soundId 代入前に発火しうるため loadLock で待ち合わせる。
        var soundId = 0
        soundPool.setOnLoadCompleteListener { _, loadedId, status ->
            val expectedId = synchronized(loadLock) { soundId }
            if (loadedId == expectedId) {
                soundPool.setOnLoadCompleteListener(null)
                if (cont.isActive) {
                    if (status == 0) {
                        cont.resume(loadedId)
                    } else {
                        captureNarratorError(IllegalStateException("SoundPool load failed: status=$status"))
                        soundPool.unload(loadedId)
                        cont.resume(0)
                    }
                }
            }
        }
        synchronized(loadLock) { soundId = soundPool.load(path, 1) }
        cont.invokeOnCancellation {
            synchronized(loadLock) { soundId }.takeIf { it != 0 }?.let { soundPool.unload(it) }
        }
    }

    private companion object {
        const val LOAD_TIMEOUT_MS = 5_000L

        fun wavDurationMs(bytes: ByteArray): Long {
            if (bytes.size < 44) return 0L
            val byteRate = bytes.readInt32LE(28)
            if (byteRate <= 0) return 0L
            var offset = 12
            while (offset + 8 <= bytes.size) {
                val chunkId = String(bytes, offset, 4, Charsets.US_ASCII)
                val chunkSize = bytes.readInt32LE(offset + 4)
                if (chunkId == "data") return chunkSize.toLong() * 1000L / byteRate
                offset += 8 + chunkSize
            }
            return 0L
        }

        fun ByteArray.readInt32LE(offset: Int): Int =
            (this[offset].toInt() and 0xFF) or
                ((this[offset + 1].toInt() and 0xFF) shl 8) or
                ((this[offset + 2].toInt() and 0xFF) shl 16) or
                ((this[offset + 3].toInt() and 0xFF) shl 24)
    }
}
