package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey

internal data class ReadoutListUiState(
    val selectedSimulator: String? = null,
    val simulators: List<String> = emptyList(),
    val items: List<ReadoutItemKey> = emptyList(),
    val readoutEnabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val selectedItem: ReadoutListItemType? = null,
)
