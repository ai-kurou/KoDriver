package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import androidx.lifecycle.ViewModel
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase

internal class LmuWindowsReadoutTyreWearDetailViewModel(
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {

    fun onWarningChipClicked() {
        playSpeechEvent(SpeechEvent.TyreWearWarning)
    }
}
