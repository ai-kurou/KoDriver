package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.formatLmuWindowsBrakeTemperatureReadoutText
import kurou.kodriver.domain.model.lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.preview.ReadoutSpeechEventPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
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

internal class LmuWindowsReadoutBrakeTemperatureDetailViewModel(
    private val brakeTemperatureUseCases: BrakeTemperatureUseCases,
    observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    private val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
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

    val uiState: StateFlow<LmuWindowsReadoutBrakeTemperatureDetailUiState> =
        combine(
            brakeTemperatureUseCases.observeVehicleClassHighThreshold(),
            brakeTemperatureUseCases.observeVehicleClassSelection(),
            observeReadoutEnabledStates(Simulator.LmuWindows.id),
            brakeTemperatureUseCases.observeText(),
            previewHelper.textToSpeechAvailable,
        ) { vehicleClassHighThresholdCelsius, selectedVehicleClass, enabledStates, text, available ->
            LmuWindowsReadoutBrakeTemperatureDetailUiState(
                vehicleClassHighThresholdCelsius = vehicleClassHighThresholdCelsius,
                selectedVehicleClass = selectedVehicleClass,
                readoutText = text,
                isTextToSpeechAvailable = available,
                enabled = enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutBrakeTemperatureDetailUiState(),
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
                SpeechEvent.LmuWindowsBrakeOverheat(
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
            saveReadoutEnabledState(
                Simulator.LmuWindows.id,
                LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout,
                enabled,
            )
        }
    }
}
