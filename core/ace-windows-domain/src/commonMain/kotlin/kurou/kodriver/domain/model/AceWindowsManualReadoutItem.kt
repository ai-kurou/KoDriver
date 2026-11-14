package kurou.kodriver.domain.model

import kurou.kodriver.domain.acewindows.model.AceWindowsGapData

/** 鮮度判定済みの ACE 手動読み上げ項目。[value] は項目キーに対応する種別の現在値で、未受信・鮮度切れ・不正値は null。
 * 前後車ギャップだけは前後2つの値を持つため [gap] に入れ、未受信・鮮度切れは null。 */
data class AceWindowsManualReadoutItem(
    val key: String,
    val textTemplate: String,
    val value: ManualReadoutValue?,
    val gap: AceWindowsGapData? = null,
)
