package kurou.kodriver.feature.readout

data class ReadoutListUiState(
    val selectedSimulator: String? = null,
    val simulators: List<String> = emptyList(),
    val items: List<String> = emptyList(),
    val switchStates: Map<String, Boolean> = emptyMap(),
)
