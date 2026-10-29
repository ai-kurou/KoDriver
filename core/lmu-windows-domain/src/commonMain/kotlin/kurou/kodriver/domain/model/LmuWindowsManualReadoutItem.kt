package kurou.kodriver.domain.model

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData

/** 鮮度判定済みの手動読み上げ項目。残存率は0〜1、温度は℃、ラップタイムはms、残り周回数は整数、クラス内ギャップはデータで、未受信・鮮度切れは null。 */
data class LmuWindowsManualReadoutItem(
    val key: String,
    val textTemplate: String,
    val ratio: Double?,
    val celsius: Float? = null,
    val lapTimeMs: Long? = null,
    val remainingLaps: Int? = null,
    val classCarGap: LmuWindowsClassCarGapData? = null,
)

/** 摂氏温度を使う手動読み上げ項目かを判定する。 */
fun isLmuWindowsManualReadoutTemperatureItem(itemKey: String): Boolean =
    itemKey == LmuWindowsReadoutItemKey.BrakeTemperature.Root.value ||
        itemKey == LmuWindowsReadoutItemKey.TyreTemperature.Root.value
