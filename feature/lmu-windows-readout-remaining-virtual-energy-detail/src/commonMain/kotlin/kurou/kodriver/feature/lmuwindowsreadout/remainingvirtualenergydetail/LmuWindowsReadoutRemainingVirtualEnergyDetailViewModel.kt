package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase

/**
 * バーチャルエナジー残量アナウンス詳細設定の ViewModel。
 * 現時点では設定項目を持たず、試聴チップの音声再生のみを提供する。
 */
internal class LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel(
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutRemainingVirtualEnergyDetailUiState> =
        MutableStateFlow(LmuWindowsReadoutRemainingVirtualEnergyDetailUiState).asStateFlow()

    fun onWarningChipClicked() {
        playSpeechEvent(SpeechEvent.RemainingVirtualEnergyWarning)
    }
}
