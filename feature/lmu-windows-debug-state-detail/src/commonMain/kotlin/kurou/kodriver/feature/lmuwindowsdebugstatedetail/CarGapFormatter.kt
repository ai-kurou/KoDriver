package kurou.kodriver.feature.lmuwindowsdebugstatedetail

/** 時間差を小数1桁に整形し、null は車両なし、非有限値は取得不能として表示する。 */
internal fun formatCarGapSeconds(seconds: Double?): String =
    when {
        seconds == null -> "なし"
        !seconds.isFinite() -> "--"
        else -> formatOneDecimal(seconds)
    }
