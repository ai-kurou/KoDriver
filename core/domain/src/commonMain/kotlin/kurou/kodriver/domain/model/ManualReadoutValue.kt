package kurou.kodriver.domain.model

/** 手動読み上げ項目の現在値。単位は種別ごとに固定で、項目キーと値の種別の対応は各シミュレーターのユースケースが判定する。 */
sealed interface ManualReadoutValue {
    val value: Int

    /** 残量などの割合（％）。 */
    data class Percent(
        override val value: Int,
    ) : ManualReadoutValue

    /** 摂氏温度（℃）。 */
    data class Celsius(
        override val value: Int,
    ) : ManualReadoutValue

    /** ラップタイム（ms）。 */
    data class LapTimeMs(
        override val value: Int,
    ) : ManualReadoutValue

    /** 残り周回数。 */
    data class RemainingLaps(
        override val value: Int,
    ) : ManualReadoutValue
}
