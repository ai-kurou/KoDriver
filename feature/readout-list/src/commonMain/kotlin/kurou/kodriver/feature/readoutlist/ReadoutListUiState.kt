package kurou.kodriver.feature.readoutlist

internal data class ReadoutListUiState(
    val selectedSimulator: String? = null,
    val simulators: List<String> = emptyList(),
    val items: List<String> = emptyList(),
    val readoutEnabledStates: Map<String, Boolean> = emptyMap(),
    val selectedItem: ReadoutListItemType? = null,
)
