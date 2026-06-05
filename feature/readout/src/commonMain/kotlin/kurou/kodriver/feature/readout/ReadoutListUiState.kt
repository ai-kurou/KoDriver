package kurou.kodriver.feature.readout

import kurou.kodriver.domain.model.ReadoutItemType

internal data class ReadoutListUiState(
    val selectedSimulator: String? = null,
    val simulators: List<String> = emptyList(),
    val items: List<String> = emptyList(),
    val readoutEnabledStates: Map<String, Boolean> = emptyMap(),
    val selectedItem: ReadoutItemType? = null,
)
