package kurou.kodriver.domain.acewindows.model

import kotlinx.serialization.Serializable

/**
 * Assetto Corsa EVO の Windows 共有メモリから読み取った前車・後車とのギャップ。
 * 符号付きの値を保持し、前車・後車がいない場合も共有メモリの生値をそのまま返す。
 */
@Serializable
data class AceWindowsGapData(
    /** 前車とのギャップ。単位は秒。 */
    val gapAheadSeconds: Float,
    /** 後車とのギャップ。単位は秒。 */
    val gapBehindSeconds: Float,
    /** デバッグ用: `active_cars`（+3852）の生値。ギャップのオフセット検証に使う。 */
    val debugActiveCars: Int = 0,
    /** デバッグ用: `gap_ahead` 付近を 4 バイトずつ読んだ生の整数列（先頭は [GAP_DEBUG_WINDOW_START_OFFSET]）。 */
    val debugRawWords: List<Int> = emptyList(),
) {
    companion object {
        const val GAP_DEBUG_WINDOW_START_OFFSET = 3820
    }
}
