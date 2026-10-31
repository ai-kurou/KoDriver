package kurou.kodriver.domain.model

/** 鮮度判定済みの ACE 手動読み上げ項目。[value] は項目キーに対応する種別の現在値で、未受信・鮮度切れ・不正値は null。 */
data class AceWindowsManualReadoutItem(
    val key: String,
    val textTemplate: String,
    val value: ManualReadoutValue?,
)
