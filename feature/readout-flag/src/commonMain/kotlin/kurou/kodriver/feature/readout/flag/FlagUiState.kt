package kurou.kodriver.feature.readout.flag

data class FlagUiState(
    val enabledStates: Map<String, Boolean> = emptyMap(),
)
