package kurou.kodriver.feature.lmuwindowsnarrator

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

class AndroidSoundPlayer(private val context: Context) : SoundPlayer {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        )
        .build()

    private var currentStreamId: Int = 0

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

    private suspend fun loadSound(path: String): Int = suspendCancellableCoroutine { cont ->
        val soundId = soundPool.load(path, 1)
        soundPool.setOnLoadCompleteListener { _, loadedId, status ->
            if (loadedId == soundId) {
                soundPool.setOnLoadCompleteListener(null)
                if (cont.isActive) {
                    if (status == 0) {
                        cont.resume(soundId)
                    } else {
                        captureNarratorError(IllegalStateException("SoundPool load failed: status=$status"))
                        soundPool.unload(soundId)
                        cont.resume(0)
                    }
                }
            }
        }
        cont.invokeOnCancellation { soundPool.unload(soundId) }
    }

    private companion object {
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
