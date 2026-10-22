package kurou.kodriver.domain.model

/** 鮮度判定済みの ACE 手動読み上げ項目。残り燃料は整数％、未受信・鮮度切れ・不正値は null。 */
data class AceWindowsManualReadoutItem(
    val key: String,
    val textTemplate: String,
    val value: Int?,
)
