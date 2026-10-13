package kurou.kodriver.feature.lmuwindowsautoreadout.remainingvirtualenergydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.formatLmuWindowsRemainingVirtualEnergyReadoutText
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class RemainingVirtualEnergyUseCases(
    val observeThresholdPercentage: ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase,
    val saveThresholdPercentage: SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase,
    val observeAutoReadoutEnabledStates: ObserveAutoReadoutEnabledStatesUseCase,
    val saveAutoReadoutEnabledState: SaveAutoReadoutEnabledStateUseCase,
)

internal data class RemainingVirtualEnergyReadoutUseCases(
    val observeText: ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase,
    val saveText: SaveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class LmuWindowsAutoReadoutRemainingVirtualEnergyDetailViewModel(
    private val remainingVirtualEnergyUseCases: RemainingVirtualEnergyUseCases,
    private val readout: RemainingVirtualEnergyReadoutUseCases,
) : ViewModel() {
    private val preview =
        ReadoutTextPreviewHelper(
            viewModelScope,
            readout.checkTextToSpeechAvailable,
            readout.observeSoundVolume,
            readout.playStartSoundForKey,
            readout.speakText,
        )

    val uiState: StateFlow<LmuWindowsAutoReadoutRemainingVirtualEnergyDetailUiState> =
        combine(
            remainingVirtualEnergyUseCases.observeThresholdPercentage(),
            remainingVirtualEnergyUseCases.observeAutoReadoutEnabledStates(Simulator.LmuWindows.id),
            readout.observeText(),
            preview.textToSpeechAvailable,
        ) { thresholdPercentage, enabledStates, text, available ->
            LmuWindowsAutoReadoutRemainingVirtualEnergyDetailUiState(
                thresholdPercentage = thresholdPercentage,
                readoutText = text,
                isTextToSpeechAvailable = available,
                enabled = enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.WarningReadout),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsAutoReadoutRemainingVirtualEnergyDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveText(text) }
    }

    /** 現在の閾値に置換し、空白文言・TTS利用不可・音量ゼロでは再生しない。 */
    fun onReadoutTextPreviewClicked(text: String) {
        val formattedText = formatLmuWindowsRemainingVirtualEnergyReadoutText(text, uiState.value.thresholdPercentage)
        preview.onPreviewClicked(formattedText, LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)
    }

    fun onThresholdChanged(percentage: Int) {
        viewModelScope.launch { remainingVirtualEnergyUseCases.saveThresholdPercentage(percentage) }
    }

    fun onThresholdReset() {
        viewModelScope.launch {
            remainingVirtualEnergyUseCases.saveThresholdPercentage(
                LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT,
            )
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            remainingVirtualEnergyUseCases.saveAutoReadoutEnabledState(
                Simulator.LmuWindows.id,
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.WarningReadout,
                enabled,
            )
        }
    }
}
