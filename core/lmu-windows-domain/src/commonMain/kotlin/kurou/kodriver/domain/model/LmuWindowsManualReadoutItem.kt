package kurou.kodriver.domain.model

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData

/** 鮮度判定済みの手動読み上げ項目。[value] は項目キーに対応する種別の現在値で、未受信・鮮度切れは null。 */
public data class LmuWindowsManualReadoutItem(
    val key: String,
    val textTemplate: String,
    val value: LmuWindowsManualReadoutValue?,
)

/** 手動読み上げ項目の現在値。単位は種別ごとに固定で、項目の追加で項目側のフィールドを増やさずに済む。 */
public sealed interface LmuWindowsManualReadoutValue {
    /** 残存率（0〜1）。 */
    public data class Ratio(
        val value: Double,
    ) : LmuWindowsManualReadoutValue

    /** 摂氏温度（℃）。 */
    public data class Celsius(
        val value: Float,
    ) : LmuWindowsManualReadoutValue

    /** ラップタイム（ms）。 */
    public data class LapTimeMs(
        val value: Long,
    ) : LmuWindowsManualReadoutValue

    /** 残り周回数。 */
    public data class RemainingLaps(
        val value: Int,
    ) : LmuWindowsManualReadoutValue

    /** クラス内前後車とのギャップ（データ）。 */
    public data class ClassCarGap(
        val data: LmuWindowsClassCarGapData,
    ) : LmuWindowsManualReadoutValue
}

/** 摂氏温度を使う手動読み上げ項目かを判定する。 */
public fun isLmuWindowsManualReadoutTemperatureItem(itemKey: String): Boolean =
    itemKey == LmuWindowsReadoutItemKey.BrakeTemperature.Root.value ||
        itemKey == LmuWindowsReadoutItemKey.TyreTemperature.Root.value

/** クラス内前後車とのギャップを使う手動読み上げ項目かを判定する。 */
public fun isLmuWindowsManualReadoutClassCarGapItem(itemKey: String): Boolean =
    itemKey == LmuWindowsReadoutItemKey.ClassCarGap.Root.value
