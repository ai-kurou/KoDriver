package kurou.kodriver.domain.model

/** シミュレーターの手動読み上げ項目を1つだけ読み上げる依頼。音声認識の発話から発行する。 */
data class ManualReadoutItemRequest(
    val simulatorId: String,
    val itemKey: String,
)
