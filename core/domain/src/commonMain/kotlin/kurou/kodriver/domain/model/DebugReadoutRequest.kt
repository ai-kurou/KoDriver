package kurou.kodriver.domain.model

/** テレメトリの読み上げ判定を介さず Narrator に渡すデバッグ要求。 */
data class DebugReadoutRequest(
    val simulator: Simulator,
    val readoutItemKey: ReadoutItemKey,
    val text: String,
)
