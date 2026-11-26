package kurou.kodriver.domain.debug.model

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

/** テレメトリの読み上げ判定を介さず Narrator に渡すデバッグ要求。 */
public data class DebugReadoutRequest(
    val simulator: Simulator,
    val readoutItemKey: ReadoutItemKey,
    val text: String,
)
