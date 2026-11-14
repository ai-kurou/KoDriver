package kurou.kodriver.feature.acewindowsdebugstatedetail

import kurou.kodriver.domain.util.formatOneDecimal

/** 時間差の符号を保持して小数1桁に整形し、非有限値は欠損として表示する。 */
internal fun formatCarGapSeconds(seconds: Float): String =
    if (seconds.isFinite()) formatOneDecimal(seconds.toDouble()) else "--"

/** 総合順位・総台数を整形し、未受信または未確定の値は欠損表示にする。 */
internal fun formatOverallPosition(value: Int?): String = value?.takeIf { it > 0 }?.toString() ?: "--"

/** デバッグ用: 4 バイトの生値を「+オフセット float値 / 0x16進」の1行に整形する（オフセット検証用の一時表示）。 */
internal fun formatGapDebugWord(
    offset: Int,
    raw: Int,
): String {
    val hex =
        raw
            .toUInt()
            .toString(HEX_RADIX)
            .uppercase()
            .padStart(HEX_WORD_DIGITS, '0')
    return "+$offset ${Float.fromBits(raw)} / 0x$hex"
}

private const val HEX_RADIX = 16
private const val HEX_WORD_DIGITS = 8
