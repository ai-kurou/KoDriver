package kurou.kodriver.feature.lmuwindowsdebugstatedetail

/** 時間差の符号を保持して小数1桁に整形し、null・非有限値は欠損として表示する。 */
internal fun formatCarGapSeconds(seconds: Double?): String =
    if (seconds != null && seconds.isFinite()) formatOneDecimal(seconds) else "--"
