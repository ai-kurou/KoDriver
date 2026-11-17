package kurou.kodriver.feature.otherlist

public data class OtherListTtsAvailabilityUiState(
    val guidanceItems: List<OtherListItemType> = emptyList(),
    val ttsUnavailableGuidance: TtsUnavailableGuidance? = null,
)
