package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.formatAceWindowsRemainingFuelReadoutText
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class RemainingFuelUseCases(
    val observeThresholdPercentage: ObserveAceWindowsRemainingFuelThresholdPercentageUseCase,
    val saveThresholdPercentage: SaveAceWindowsRemainingFuelThresholdPercentageUseCase,
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
)

internal data class RemainingFuelReadoutUseCases(
    val observeText: ObserveAceWindowsRemainingFuelReadoutTextUseCase,
    val saveText: SaveAceWindowsRemainingFuelReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class AceWindowsReadoutRemainingFuelDetailViewModel(
    private val remainingFuelUseCases: RemainingFuelUseCases,
    private val readout: RemainingFuelReadoutUseCases,
) : ViewModel() {
    private val preview =
        ReadoutTextPreviewHelper(
            viewModelScope,
            readout.checkTextToSpeechAvailable,
            readout.observeSoundVolume,
            readout.playStartSoundForKey,
            readout.speakText,
        )

    val uiState: StateFlow<AceWindowsReadoutRemainingFuelDetailUiState> =
        combine(
            remainingFuelUseCases.observeThresholdPercentage(),
            remainingFuelUseCases.observeReadoutEnabledStates(Simulator.AceWindows.id),
            readout.observeText(),
            preview.textToSpeechAvailable,
        ) { thresholdPercentage, enabledStates, text, available ->
            AceWindowsReadoutRemainingFuelDetailUiState(
                thresholdPercentage = thresholdPercentage,
                readoutText = text,
                isTextToSpeechAvailable = available,
                enabled = enabledStates.readoutEnabled(ReadoutItemKey.AceWindows.RemainingFuel.DetailEnabled),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AceWindowsReadoutRemainingFuelDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveText(text) }
    }

    /** 保存反映待ちの旧値を避けるため、画面に表示中の残量閾値を呼び出し側から受け取る。 */
    fun onReadoutTextPreviewClicked(
        text: String,
        percent: Int,
    ) {
        val resolvedText = formatAceWindowsRemainingFuelReadoutText(text, percent)
        preview.onPreviewClicked(resolvedText, ReadoutItemKey.AceWindows.RemainingFuel.Root)
    }

    fun onThresholdChanged(percentage: Int) {
        viewModelScope.launch { remainingFuelUseCases.saveThresholdPercentage(percentage) }
    }

    fun onThresholdReset() {
        onThresholdChanged(ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            remainingFuelUseCases.saveReadoutEnabledState(
                Simulator.AceWindows.id,
                ReadoutItemKey.AceWindows.RemainingFuel.DetailEnabled,
                enabled,
            )
        }
    }
}
