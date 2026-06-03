package kurou.kodriver.feature.readout

data class ReadoutListUiState(
    val selectedSimulator: String? = null,
    val simulators: List<String> = emptyList(),
    val simulatorDisplayNames: Map<String, String> = emptyMap(),
    val items: List<String> = emptyList(),
    val readoutEnabledStates: Map<String, Boolean> = emptyMap(),
)
