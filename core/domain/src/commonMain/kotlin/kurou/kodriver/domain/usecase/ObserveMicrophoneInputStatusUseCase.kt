package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.model.MicrophoneInputStatus
import kurou.kodriver.domain.repository.MicrophoneRepository
import kotlin.math.sqrt

/** 16bit PCM の最大振幅。音量の正規化に使う。 */
private const val PCM_16BIT_MAX_AMPLITUDE = 32_768f

/** 16bit PCM の1サンプルのバイト数。 */
private const val BYTES_PER_SAMPLE = 2

private const val BYTE_MASK = 0xFF

/**
 * マイクの入力状態（開始待ち・音量・失敗理由）を流す。収集を始めるとマイクを開き、収集をやめると閉じる。
 * 開始直後に [MicrophoneInputStatus.Starting] を流し、以降は音声の断片ごとに音量を計算して流す。
 */
class ObserveMicrophoneInputStatusUseCase(
    private val repository: MicrophoneRepository,
) {
    operator fun invoke(device: AudioInputDevice? = null): Flow<MicrophoneInputStatus> =
        repository
            .observeAudio(device)
            .map { event ->
                when (event) {
                    is MicrophoneAudioEvent.Chunk -> MicrophoneInputStatus.Capturing(event.pcm.rmsLevel())
                    is MicrophoneAudioEvent.Failed -> MicrophoneInputStatus.Failed(event.reason)
                }
            }.onStart { emit(MicrophoneInputStatus.Starting) }
}

/** リトルエンディアン符号付き16bit PCM の RMS を 0.0〜1.0 に正規化する。端数のバイトは無視する。 */
private fun ByteArray.rmsLevel(): Float {
    val sampleCount = size / BYTES_PER_SAMPLE
    if (sampleCount == 0) return 0f
    var sumOfSquares = 0.0
    for (i in 0 until sampleCount) {
        val low = this[i * BYTES_PER_SAMPLE].toInt() and BYTE_MASK
        val high = this[i * BYTES_PER_SAMPLE + 1].toInt() shl Byte.SIZE_BITS
        val sample = (high or low).toShort()
        sumOfSquares += sample.toDouble() * sample
    }
    return (sqrt(sumOfSquares / sampleCount).toFloat() / PCM_16BIT_MAX_AMPLITUDE).coerceIn(0f, 1f)
}
