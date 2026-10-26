package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.usecase.CheckTextToSpeechUnavailableReasonUseCase

/** TTS利用可否の判定と一覧項目・警告表示への反映を管理する。実行のライフサイクルは呼び出し元に従う。 */
class OtherListTtsAvailabilityFlow(
    private val checkUnavailableReason: CheckTextToSpeechUnavailableReasonUseCase,
) {
    private val _uiState = MutableStateFlow(OtherListTtsAvailabilityUiState())
    val uiState: StateFlow<OtherListTtsAvailabilityUiState> = _uiState.asStateFlow()

    suspend fun checkAvailability() {
        val reason = checkUnavailableReason()
        _uiState.update {
            OtherListTtsAvailabilityUiState(
                guidanceItems = reason.toGuidanceItems(),
                ttsUnavailableGuidance = reason?.toGuidance(),
            )
        }
    }

    fun applyGuidance(items: List<OtherListItemType>): List<OtherListItemType> =
        (items.filterNot { it.isTtsGuidanceItem() } + uiState.value.guidanceItems).sortedBy { it.ordinal }
}

private fun TextToSpeechUnavailableReason?.toGuidanceItems(): List<OtherListItemType> {
    val guidanceItem =
        when (this) {
            TextToSpeechUnavailableReason.EngineMissing -> OtherListItemType.TtsEngineMissing
            TextToSpeechUnavailableReason.LanguageDataMissing -> OtherListItemType.TtsLanguageDataMissing
            TextToSpeechUnavailableReason.WindowsSpeechUnavailable -> OtherListItemType.WindowsSpeechUnavailable
            null -> null
        }
    return listOfNotNull(guidanceItem).sortedBy { it.ordinal }
}

private fun TextToSpeechUnavailableReason?.toGuidance(): TtsUnavailableGuidance? =
    when (this) {
        TextToSpeechUnavailableReason.EngineMissing -> TtsUnavailableGuidance.EngineMissing
        TextToSpeechUnavailableReason.LanguageDataMissing -> TtsUnavailableGuidance.LanguageDataMissing
        TextToSpeechUnavailableReason.WindowsSpeechUnavailable -> TtsUnavailableGuidance.WindowsSpeechUnavailable
        null -> null
    }

private fun OtherListItemType.isTtsGuidanceItem(): Boolean =
    this == OtherListItemType.WindowsSpeechUnavailable ||
        this == OtherListItemType.TtsEngineMissing ||
        this == OtherListItemType.TtsLanguageDataMissing
