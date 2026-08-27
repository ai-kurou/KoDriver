package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SELECTED_SIMULATOR_DEFAULT
import kurou.kodriver.domain.model.Simulator

/**
 * ReadoutList 画面の表示状態。
 */
data class ReadoutListUiState(
    val selectedSimulator: Simulator = SELECTED_SIMULATOR_DEFAULT,
    val items: List<ReadoutItemKey> = emptyList(),
    val readoutEnabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val queueEnabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val startSoundEnabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val selectedItem: ReadoutListItemType? = null,
)
