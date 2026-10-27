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
)
