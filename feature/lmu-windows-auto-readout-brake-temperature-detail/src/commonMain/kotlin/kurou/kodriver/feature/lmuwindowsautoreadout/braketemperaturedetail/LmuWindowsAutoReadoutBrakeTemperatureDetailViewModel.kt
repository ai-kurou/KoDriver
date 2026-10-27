package kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.LmuWindowsBrakeOverheat
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsBrakeTemperatureReadoutText
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.preview.ReadoutSpeechEventPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.StopSpeechUseCase

internal data class BrakeTemperatureUseCases(
    val observeVehicleClassHighThreshold: ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase,
    val observeVehicleClassSelection: ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase,
    val saveVehicleClassHighThreshold: SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase,
    val saveVehicleClassSelection: SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase,
    val observeText: ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase,
    val saveText: SaveLmuWindowsBrakeTemperatureReadoutTextUseCase,
)

internal data class BrakeTemperatureReadoutUseCases(
    val playSpeechEvent: PlaySpeechEventUseCase,
    val stopSpeech: StopSpeechUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class LmuWindowsAutoReadoutBrakeTemperatureDetailViewModel(
    private val brakeTemperatureUseCases: BrakeTemperatureUseCases,
    observeAutoReadoutEnabledStates: ObserveAutoReadoutEnabledStatesUseCase,
    private val saveAutoReadoutEnabledState: SaveAutoReadoutEnabledStateUseCase,
    readout: BrakeTemperatureReadoutUseCases,
) : ViewModel() {
    private val previewHelper =
        ReadoutSpeechEventPreviewHelper(
            viewModelScope,
            readout.checkTextToSpeechAvailable,
            readout.observeSoundVolume,
            readout.playSpeechEvent,
            readout.stopSpeech,
        )

    /** 詳細ペインを離れると試聴を止める。 */
    fun onPreviewStopped() = previewHelper.stop()

    val uiState: StateFlow<LmuWindowsAutoReadoutBrakeTemperatureDetailUiState> =
        LmuWindowsAutoReadoutBrakeTemperatureDetailFlow(
            vehicleClassHighThreshold = brakeTemperatureUseCases.observeVehicleClassHighThreshold(),
            vehicleClassSelection = brakeTemperatureUseCases.observeVehicleClassSelection(),
            enabledStates = observeAutoReadoutEnabledStates(Simulator.LmuWindows.id),
            readoutText = brakeTemperatureUseCases.observeText(),
            isTextToSpeechAvailable = previewHelper.textToSpeechAvailable,
        ).uiState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsAutoReadoutBrakeTemperatureDetailUiState(),
        )

    fun onReadoutTextChanged(text: String) {
        viewModelScope.launch { brakeTemperatureUseCases.saveText(text) }
    }

    /** 現在の閾値に置換し、空白文言・TTS利用不可・音量ゼロでは再生しない。 */
    fun onReadoutTextPreviewClicked(text: String) {
        val state = uiState.value
        val celsius =
            state.vehicleClassHighThresholdCelsius[state.selectedVehicleClass]
                ?: lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(state.selectedVehicleClass)
        val formattedText = formatLmuWindowsBrakeTemperatureReadoutText(text, celsius)
        viewModelScope.launch {
            previewHelper.preview(
                formattedText,
                LmuWindowsBrakeOverheat(
                    celsius = celsius,
                    resolvedText = formattedText,
                ),
            )
        }
    }

    fun onVehicleClassSelected(vehicleClass: LmuWindowsVehicleClassData) {
        viewModelScope.launch { brakeTemperatureUseCases.saveVehicleClassSelection(vehicleClass) }
    }

    fun onVehicleClassHighThresholdChanged(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Int,
    ) {
        viewModelScope.launch {
            brakeTemperatureUseCases.saveVehicleClassHighThreshold(vehicleClass, celsius)
        }
    }

    fun onVehicleClassHighThresholdReset(vehicleClass: LmuWindowsVehicleClassData) {
        viewModelScope.launch {
            brakeTemperatureUseCases.saveVehicleClassHighThreshold(
                vehicleClass,
                lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(vehicleClass),
            )
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveAutoReadoutEnabledState(
                Simulator.LmuWindows.id,
                LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout,
                enabled,
            )
        }
    }
}
