package kurou.kodriver.feature.readoutlist

import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.Simulator

/**
 * ReadoutList 画面の表示状態。
 */
data class ReadoutListUiState(
    val selectedSimulator: Simulator? = null,
    val simulators: List<Simulator> = emptyList(),
    val items: List<ReadoutItemKey> = emptyList(),
    val readoutEnabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val queueEnabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val selectedItem: ReadoutListItemType? = null,
)
