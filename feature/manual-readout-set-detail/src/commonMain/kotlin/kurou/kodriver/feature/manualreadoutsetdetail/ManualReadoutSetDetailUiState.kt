package kurou.kodriver.feature.manualreadoutsetdetail

internal data class ManualReadoutSetDetailUiState(
    val setNumber: Int? = null,
    val availableItemKeys: List<String> = emptyList(),
    val selectedItemKeys: List<String> = emptyList(),
    val itemTexts: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val deleted: Boolean = false,
)
