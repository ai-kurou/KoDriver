package kurou.kodriver.core.speechrecognitiondata.speech

import kotlin.math.PI
import kotlin.math.sin

/** 確認音のサンプリング周波数（Hz）。 */
internal const val WAKE_WORD_CHIME_SAMPLE_RATE = 44_100

private const val FIRST_TONE_HZ = 880.0
private const val SECOND_TONE_HZ = 1_318.5
private const val FIRST_TONE_MS = 90
private const val SECOND_TONE_MS = 130
private const val FADE_MS = 8

/** 振幅（満振幅に対する割合）。大きすぎて驚かせず、走行中でも聞き取れる程度にする。 */
private const val AMPLITUDE = 0.35

/**
 * ウェイクワードを検出したときの確認音（短い上昇2音、モノラル16ビットPCM）を生成する。
 *
 * 読み上げの開始音（WAV）とは別の音にするため、資源を持たず波形を計算で作る。
 * 各音の頭と終わりを [FADE_MS] ミリ秒かけて振幅を上げ下げし、継ぎ目のクリックノイズを避ける。
 */
internal fun wakeWordChimePcm(): ShortArray = tone(FIRST_TONE_HZ, FIRST_TONE_MS) + tone(SECOND_TONE_HZ, SECOND_TONE_MS)

/** [wakeWordChimePcm] の再生時間（ミリ秒）。 */
internal const val WAKE_WORD_CHIME_DURATION_MS = FIRST_TONE_MS + SECOND_TONE_MS

private fun tone(
    frequencyHz: Double,
    durationMs: Int,
): ShortArray {
    val samples = WAKE_WORD_CHIME_SAMPLE_RATE * durationMs / 1_000
    val fadeSamples = WAKE_WORD_CHIME_SAMPLE_RATE * FADE_MS / 1_000
    return ShortArray(samples) { i ->
        val fade = minOf(1.0, i.toDouble() / fadeSamples, (samples - 1 - i).toDouble() / fadeSamples)
        val value = sin(2.0 * PI * frequencyHz * i / WAKE_WORD_CHIME_SAMPLE_RATE) * AMPLITUDE * fade
        (value * Short.MAX_VALUE).toInt().toShort()
    }
}
