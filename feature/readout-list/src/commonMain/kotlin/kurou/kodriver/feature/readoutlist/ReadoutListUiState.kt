package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

data class ReadoutListUiState(
    val selectedSimulator: Simulator? = null,
    val simulators: List<Simulator> = emptyList(),
    val items: List<ReadoutItemKey> = emptyList(),
    val readoutEnabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val selectedItem: ReadoutListItemType? = null,
)
