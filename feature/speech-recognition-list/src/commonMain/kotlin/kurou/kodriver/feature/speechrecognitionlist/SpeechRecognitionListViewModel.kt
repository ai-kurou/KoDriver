package kurou.kodriver.feature.speechrecognitionlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.ObserveSpeechCommandGuideUseCase

/** 選択中のシミュレーターの「読み上げ項目と語句」の案内を観測し、一覧の表示状態にする。 */
internal class SpeechRecognitionListViewModel(
    observeSpeechCommandGuide: ObserveSpeechCommandGuideUseCase,
) : ViewModel() {
    val uiState =
        observeSpeechCommandGuide()
            .map { SpeechRecognitionListUiState(groups = it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpeechRecognitionListUiState())
}
