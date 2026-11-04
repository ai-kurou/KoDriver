package kurou.kodriver.data.microphone

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.MICROPHONE_CHANNEL_COUNT
import kurou.kodriver.domain.model.MICROPHONE_SAMPLE_RATE_HZ
import kurou.kodriver.domain.model.MICROPHONE_SAMPLE_SIZE_BITS
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.model.MicrophoneFailureReason
import kurou.kodriver.domain.repository.MicrophoneRepository

private const val MILLIS_PER_SECOND = 1_000
private const val CHUNK_MILLIS = 100
private const val SILENCE_LIMIT_MILLIS = 5_000
private const val BYTES_PER_SECOND =
    MICROPHONE_SAMPLE_RATE_HZ * MICROPHONE_CHANNEL_COUNT * MICROPHONE_SAMPLE_SIZE_BITS / Byte.SIZE_BITS
private const val CHUNK_BYTES = BYTES_PER_SECOND * CHUNK_MILLIS / MILLIS_PER_SECOND
private const val SILENCE_LIMIT_BYTES = BYTES_PER_SECOND * SILENCE_LIMIT_MILLIS / MILLIS_PER_SECOND

/**
 * [AudioInputSource] から読み込んだPCMを100ミリ秒ごとの断片にして流す [MicrophoneRepository]。
 *
 * 完全な無音（全バイトが0）が5秒続いたら [MicrophoneFailureReason.NoSignal] を流して完了する。
 * Windows のプライバシー設定でマイクがブロックされていると、デバイスは開けるが全ゼロの音声が届くため、
 * 権限の代わりにこれで検出する。
 */
internal class JvmMicrophoneRepository(
    private val source: AudioInputSource = JavaSoundAudioInputSource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MicrophoneRepository {
    override fun observeAudio(device: AudioInputDevice?): Flow<MicrophoneAudioEvent> =
        flow {
            val capture = source.open(device?.name)
            if (capture == null) {
                emit(MicrophoneAudioEvent.Failed(MicrophoneFailureReason.Unavailable))
            } else {
                capture.use { emitChunks(it) }
            }
        }.flowOn(ioDispatcher)

    private suspend fun FlowCollector<MicrophoneAudioEvent>.emitChunks(capture: AudioCapture) {
        val buffer = ByteArray(CHUNK_BYTES)
        var silentBytes = 0
        while (currentCoroutineContext().isActive) {
            val read = capture.read(buffer)
            if (read < 0) {
                emit(MicrophoneAudioEvent.Failed(MicrophoneFailureReason.Unavailable))
                return
            }
            if (read == 0) continue
            val pcm = buffer.copyOf(read)
            silentBytes = if (pcm.all { it == 0.toByte() }) silentBytes + read else 0
            if (silentBytes >= SILENCE_LIMIT_BYTES) {
                emit(MicrophoneAudioEvent.Failed(MicrophoneFailureReason.NoSignal))
                return
            }
            emit(MicrophoneAudioEvent.Chunk(pcm))
        }
    }
}
