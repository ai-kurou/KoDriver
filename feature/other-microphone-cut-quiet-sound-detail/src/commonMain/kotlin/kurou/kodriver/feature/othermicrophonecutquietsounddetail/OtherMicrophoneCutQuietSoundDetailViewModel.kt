package kurou.kodriver.feature.othermicrophonecutquietsounddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.launchCapturingErrors
import kurou.kodriver.domain.usecase.ObserveMicrophoneCutQuietSoundLevelUseCase
import kurou.kodriver.domain.usecase.SaveMicrophoneCutQuietSoundLevelUseCase

internal class OtherMicrophoneCutQuietSoundDetailViewModel(
    observeMicrophoneCutQuietSoundLevel: ObserveMicrophoneCutQuietSoundLevelUseCase,
    private val saveMicrophoneCutQuietSoundLevel: SaveMicrophoneCutQuietSoundLevelUseCase,
) : ViewModel() {
    val uiState: StateFlow<OtherMicrophoneCutQuietSoundDetailUiState> =
        OtherMicrophoneCutQuietSoundDetailFlow(
            observedLevel = observeMicrophoneCutQuietSoundLevel(),
        ).uiState
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                OtherMicrophoneCutQuietSoundDetailUiState(),
            )

    fun onLevelChanged(level: Int) {
        viewModelScope.launchCapturingErrors { saveMicrophoneCutQuietSoundLevel(level) }
    }
}
