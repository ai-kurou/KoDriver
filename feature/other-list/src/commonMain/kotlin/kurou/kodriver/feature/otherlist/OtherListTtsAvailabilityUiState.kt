package kurou.kodriver.feature.otherlist

data class OtherListTtsAvailabilityUiState(
    val guidanceItems: List<OtherListItemType> = emptyList(),
    val ttsUnavailableGuidance: TtsUnavailableGuidance? = null,
)
