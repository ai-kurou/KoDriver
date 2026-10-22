package kurou.kodriver.domain.model

/** 鮮度判定済みの GT7 手動読み上げ項目。残り燃料は％、残り燃料の周回数は周、タイヤ温度は℃、自己ベストラップはms。未受信・鮮度切れは null。 */
data class Gt7Ps5ManualReadoutItem(
    val key: String,
    val textTemplate: String,
    val value: Int?,
)
