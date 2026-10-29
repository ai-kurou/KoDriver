package kurou.kodriver.feature.acewindowsdebugstatedetail

/** 時間差の符号を保持して小数1桁に整形し、非有限値は欠損として表示する。 */
internal fun formatCarGapSeconds(seconds: Float): String =
    if (seconds.isFinite()) formatOneDecimal(seconds.toDouble()) else "--"

/** 総合順位・総台数を整形し、未受信または未確定の値は欠損表示にする。 */
internal fun formatOverallPosition(value: Int?): String = value?.takeIf { it > 0 }?.toString() ?: "--"
