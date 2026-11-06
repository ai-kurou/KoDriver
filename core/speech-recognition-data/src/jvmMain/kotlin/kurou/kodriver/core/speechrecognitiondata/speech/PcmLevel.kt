package kurou.kodriver.core.speechrecognitiondata.speech

import kotlin.math.log10
import kotlin.math.sqrt

/** 16bit PCM の最大振幅。 */
private const val PCM_16BIT_MAX_AMPLITUDE = 32_768.0

/** 16bit PCM の1サンプルのバイト数。 */
private const val BYTES_PER_SAMPLE = 2

private const val BYTE_MASK = 0xFF

private const val DECIBELS_PER_BEL = 20.0

/** 音量ゲージが0になる、フルスケールに対する RMS の dB。これ以下は無音として扱う。 */
internal const val PCM_LEVEL_DB_MIN = -60.0

/** 音量ゲージが最大になる、フルスケールに対する RMS の dB。 */
internal const val PCM_LEVEL_DB_MAX = -10.0

/**
 * リトルエンディアン符号付き16bit PCM の音量を 0.0（無音）〜1.0（大音量）に正規化する。端数のバイトは無視する。
 * 通常の話し声の RMS はフルスケールのごく一部のため、線形ではゲージがほとんど動かない。
 * Android の認識エンジンが通知する音量と同じように発話で十分に振れるよう、RMS を dB にしてから正規化する。
 */
internal fun ByteArray.pcmLevel(): Float {
    val sampleCount = size / BYTES_PER_SAMPLE
    if (sampleCount == 0) return 0f
    var sumOfSquares = 0.0
    for (i in 0 until sampleCount) {
        val low = this[i * BYTES_PER_SAMPLE].toInt() and BYTE_MASK
        val high = this[i * BYTES_PER_SAMPLE + 1].toInt() shl Byte.SIZE_BITS
        val sample = (high or low).toShort()
        sumOfSquares += sample.toDouble() * sample
    }
    val rms = sqrt(sumOfSquares / sampleCount) / PCM_16BIT_MAX_AMPLITUDE
    // rms が 0 のとき log10 は -Infinity になり、下限に丸められて 0 になる
    val decibels = DECIBELS_PER_BEL * log10(rms)
    return ((decibels - PCM_LEVEL_DB_MIN) / (PCM_LEVEL_DB_MAX - PCM_LEVEL_DB_MIN)).toFloat().coerceIn(0f, 1f)
}
