package kurou.kodriver.feature.lmuwindowsdebugstatedetail

/** 時間差の符号を保持して小数1桁に整形し、非有限値は欠損として表示する。 */
internal fun formatCarGapSeconds(seconds: Double): String = if (seconds.isFinite()) formatOneDecimal(seconds) else "--"
