package kurou.kodriver.domain.model

/**
 * シミュレーターの手動読み上げ項目を1つだけ読み上げる依頼。音声認識の発話から発行する。
 * [playStartSound] が false の場合、読み上げ前の開始音を鳴らさない（音声認識では既定で鳴らさない）。
 */
data class ManualReadoutItemRequest(
    val simulatorId: String,
    val itemKey: String,
    val playStartSound: Boolean = true,
)
