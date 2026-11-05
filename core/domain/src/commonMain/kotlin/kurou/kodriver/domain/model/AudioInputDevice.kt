package kurou.kodriver.domain.model

/**
 * 音声入力（マイク）として選べるデバイス。[name] はOSが返す表示名で、安定したIDではないため重複や変更があり得る。
 */
data class AudioInputDevice(
    val name: String,
)
