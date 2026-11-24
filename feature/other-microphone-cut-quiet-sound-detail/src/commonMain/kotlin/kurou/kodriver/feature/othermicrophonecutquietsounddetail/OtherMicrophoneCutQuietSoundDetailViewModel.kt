package kurou.kodriver.feature.othermicrophonecutquietsounddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.launchCapturingErrors
import kurou.kodriver.domain.usecase.ObserveMicrophoneCutQuietSoundInCommandUseCase
import kurou.kodriver.domain.usecase.ObserveMicrophoneCutQuietSoundLevelUseCase
import kurou.kodriver.domain.usecase.SaveMicrophoneCutQuietSoundInCommandUseCase
import kurou.kodriver.domain.usecase.SaveMicrophoneCutQuietSoundLevelUseCase

internal class OtherMicrophoneCutQuietSoundDetailViewModel(
    observeMicrophoneCutQuietSoundLevel: ObserveMicrophoneCutQuietSoundLevelUseCase,
    private val saveMicrophoneCutQuietSoundLevel: SaveMicrophoneCutQuietSoundLevelUseCase,
    observeMicrophoneCutQuietSoundInCommand: ObserveMicrophoneCutQuietSoundInCommandUseCase,
    private val saveMicrophoneCutQuietSoundInCommand: SaveMicrophoneCutQuietSoundInCommandUseCase,
) : ViewModel() {
    val uiState: StateFlow<OtherMicrophoneCutQuietSoundDetailUiState> =
        OtherMicrophoneCutQuietSoundDetailFlow(
            observedLevel = observeMicrophoneCutQuietSoundLevel(),
            observedCutInCommand = observeMicrophoneCutQuietSoundInCommand(),
        ).uiState
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                OtherMicrophoneCutQuietSoundDetailUiState(),
            )

    fun onLevelChanged(level: Int) {
        viewModelScope.launchCapturingErrors { saveMicrophoneCutQuietSoundLevel(level) }
    }

    fun onCutInCommandChanged(enabled: Boolean) {
        viewModelScope.launchCapturingErrors { saveMicrophoneCutQuietSoundInCommand(enabled) }
    }
}
