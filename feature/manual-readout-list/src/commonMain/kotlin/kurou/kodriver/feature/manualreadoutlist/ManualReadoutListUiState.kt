package kurou.kodriver.feature.manualreadoutlist

import kurou.kodriver.domain.model.ManualReadoutSet

/** 手動読み上げセット一覧の表示状態。 */
internal data class ManualReadoutListUiState(
    val sets: List<ManualReadoutSet> = emptyList(),
)
