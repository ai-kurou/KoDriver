package kurou.kodriver.feature.manualreadoutlist

import kurou.kodriver.domain.model.MANUAL_READOUT_PLAY_BUTTON_LAYOUT_ENABLED_DEFAULT
import kurou.kodriver.domain.model.ManualReadoutSet

/** 手動読み上げセット一覧の表示状態。 */
internal data class ManualReadoutListUiState(
    val selectedSetId: String? = null,
    val canReadSets: Boolean = false,
    val sets: List<ManualReadoutSet> = emptyList(),
    val playButtonLayoutEnabled: Boolean = MANUAL_READOUT_PLAY_BUTTON_LAYOUT_ENABLED_DEFAULT,
)
