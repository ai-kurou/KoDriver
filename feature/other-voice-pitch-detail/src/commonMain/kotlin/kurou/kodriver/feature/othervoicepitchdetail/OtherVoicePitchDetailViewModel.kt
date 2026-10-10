package kurou.kodriver.feature.othervoicepitchdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.SaveVoicePitchUseCase
import kotlin.math.roundToInt

internal class OtherVoicePitchDetailViewModel(
    observeVoicePitch: ObserveVoicePitchUseCase,
    private val saveVoicePitch: SaveVoicePitchUseCase,
) : ViewModel() {
    val uiState: StateFlow<OtherVoicePitchDetailUiState> =
        observeVoicePitch()
            .map { pitch -> OtherVoicePitchDetailUiState(pitch = pitch) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                OtherVoicePitchDetailUiState(),
            )

    fun onPitchChanged(pitch: Float) {
        viewModelScope.launch { saveVoicePitch((pitch * 10).roundToInt() / 10f) }
    }
}
