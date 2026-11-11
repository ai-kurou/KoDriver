package kurou.kodriver.feature.speechrecognitionlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.ObserveManualReadoutItemSpokenUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechCommandGuideUseCase

private const val STOP_TIMEOUT_MS = 5_000L

/**
 * 選択中のシミュレーターの「読み上げ項目と語句」の案内を観測し、一覧の表示状態にする。
 * 音声認識の語句で依頼され、実際に読み上げられた項目があれば、その語句の強調を指示する。
 * 一覧を表示していない間は購読しないため、別の画面を表示している間の読み上げでは何も起きない。
 */
internal class SpeechRecognitionListViewModel(
    observeSpeechCommandGuide: ObserveSpeechCommandGuideUseCase,
    observeItemSpoken: ObserveManualReadoutItemSpokenUseCase,
) : ViewModel() {
    private val highlights =
        observeItemSpoken()
            .filter { it.matchedPhrase != null }
            .scan<_, SpeechPhraseHighlight?>(null) { previous, request ->
                SpeechPhraseHighlight(
                    itemKey = request.itemKey,
                    phrase = requireNotNull(request.matchedPhrase),
                    sequence = (previous?.sequence ?: 0) + 1,
                )
            }

    val uiState =
        combine(observeSpeechCommandGuide(), highlights) { groups, highlight ->
            SpeechRecognitionListUiState(groups = groups, highlight = highlight)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS, replayExpirationMillis = 0),
            SpeechRecognitionListUiState(),
        )
}
