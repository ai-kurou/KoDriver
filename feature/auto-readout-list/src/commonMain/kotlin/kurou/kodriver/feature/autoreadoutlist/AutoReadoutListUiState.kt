package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SELECTED_SIMULATOR_DEFAULT
import kurou.kodriver.domain.model.Simulator

/**
 * AutoReadoutList 画面の表示状態。
 */
data class AutoReadoutListUiState(
    val selectedSimulator: Simulator = SELECTED_SIMULATOR_DEFAULT,
    val items: List<ReadoutItemKey> = emptyList(),
    val readoutEnabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val queueEnabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val startSoundEnabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val selectedItem: AutoReadoutListItemType? = null,
)
