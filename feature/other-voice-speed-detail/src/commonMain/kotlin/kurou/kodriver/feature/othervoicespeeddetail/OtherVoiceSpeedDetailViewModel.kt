package kurou.kodriver.feature.othervoicespeeddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.SaveVoiceSpeedUseCase
import kotlin.math.roundToInt

internal class OtherVoiceSpeedDetailViewModel(
    observeVoiceSpeed: ObserveVoiceSpeedUseCase,
    private val saveVoiceSpeed: SaveVoiceSpeedUseCase,
) : ViewModel() {
    val uiState: StateFlow<OtherVoiceSpeedDetailUiState> =
        observeVoiceSpeed()
            .map { speed -> OtherVoiceSpeedDetailUiState(speed = speed) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                OtherVoiceSpeedDetailUiState(),
            )

    fun onSpeedChanged(speed: Float) {
        viewModelScope.launch { saveVoiceSpeed((speed * 10).roundToInt() / 10f) }
    }
}
