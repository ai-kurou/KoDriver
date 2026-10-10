package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.formatLmuWindowsBrakeWearReadoutText
import kurou.kodriver.domain.model.lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.preview.ReadoutSpeechEventPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeWearSelectionUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBrakeWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeWearSelectionUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase

internal data class BrakeWearUseCases(
    val observeRemaining: ObserveLmuWindowsBrakeWearRemainingUseCase,
    val observeVehicleClassLowThreshold: ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase,
    val observeVehicleClassSelection: ObserveLmuWindowsVehicleClassBrakeWearSelectionUseCase,
    val saveVehicleClassLowThreshold: SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase,
    val saveVehicleClassSelection: SaveLmuWindowsVehicleClassBrakeWearSelectionUseCase,
    val observeText: ObserveLmuWindowsBrakeWearReadoutTextUseCase,
    val saveText: SaveLmuWindowsBrakeWearReadoutTextUseCase,
)

internal data class BrakeWearReadoutUseCases(
    val playSpeechEvent: PlaySpeechEventUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class LmuWindowsReadoutBrakeWearDetailViewModel(
    private val brakeWearUseCases: BrakeWearUseCases,
    observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    private val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
    readout: BrakeWearReadoutUseCases,
) : ViewModel() {
    private val previewHelper =
        ReadoutSpeechEventPreviewHelper(
            viewModelScope,
            readout.checkTextToSpeechAvailable,
            readout.observeSoundVolume,
            readout.playSpeechEvent,
        )

    private val settingsState =
        combine(
            brakeWearUseCases.observeVehicleClassLowThreshold(),
            brakeWearUseCases.observeVehicleClassSelection(),
            observeReadoutEnabledStates(Simulator.LmuWindows.id),
            brakeWearUseCases.observeText(),
            previewHelper.textToSpeechAvailable,
        ) { vehicleClassLowThresholdPercent, selectedVehicleClass, enabledStates, text, available ->
            LmuWindowsReadoutBrakeWearDetailUiState(
                vehicleClassLowThresholdPercent = vehicleClassLowThresholdPercent,
                selectedVehicleClass = selectedVehicleClass,
                readoutText = text,
                isTextToSpeechAvailable = available,
                enabled = enabledStates.readoutEnabled(ReadoutItemKey.LmuWindows.BrakeWear.WarningReadout),
            )
        }

    val uiState: StateFlow<LmuWindowsReadoutBrakeWearDetailUiState> =
        combine(settingsState, brakeWearUseCases.observeRemaining().onStart { emit(null) }) { settings, remaining ->
            settings.copy(remaining = remaining)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutBrakeWearDetailUiState(),
        )

    fun onReadoutTextChanged(text: String) {
        viewModelScope.launch { brakeWearUseCases.saveText(text) }
    }

    /** 現在の閾値に置換し、空白文言・TTS利用不可・音量ゼロでは再生しない。 */
    fun onReadoutTextPreviewClicked(text: String) {
        val state = uiState.value
        val percent =
            state.vehicleClassLowThresholdPercent[state.selectedVehicleClass]
                ?: lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(state.selectedVehicleClass)
        val formattedText = formatLmuWindowsBrakeWearReadoutText(text, percent)
        viewModelScope.launch {
            previewHelper.preview(
                formattedText,
                SpeechEvent.LmuWindowsBrakeWearLow(
                    percent = percent,
                    resolvedText = formattedText,
                ),
            )
        }
    }

    fun onVehicleClassSelected(vehicleClass: LmuWindowsVehicleClassData) {
        viewModelScope.launch { brakeWearUseCases.saveVehicleClassSelection(vehicleClass) }
    }

    fun onVehicleClassLowThresholdChanged(
        vehicleClass: LmuWindowsVehicleClassData,
        percent: Int,
    ) {
        viewModelScope.launch {
            brakeWearUseCases.saveVehicleClassLowThreshold(vehicleClass, percent)
        }
    }

    fun onVehicleClassLowThresholdReset(vehicleClass: LmuWindowsVehicleClassData) {
        viewModelScope.launch {
            brakeWearUseCases.saveVehicleClassLowThreshold(
                vehicleClass,
                lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(vehicleClass),
            )
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveReadoutEnabledState(
                Simulator.LmuWindows.id,
                ReadoutItemKey.LmuWindows.BrakeWear.WarningReadout,
                enabled,
            )
        }
    }
}
