package kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.LmuWindowsBrakeWearLow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsBrakeWearReadoutText
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassBrakeWearSelectionUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsBrakeWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleClassBrakeWearSelectionUseCase
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.preview.ReadoutSpeechEventPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.StopSpeechUseCase

internal data class BrakeWearUseCases(
    val observeVehicleClassLowThreshold: ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase,
    val observeVehicleClassSelection: ObserveLmuWindowsVehicleClassBrakeWearSelectionUseCase,
    val saveVehicleClassLowThreshold: SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase,
    val saveVehicleClassSelection: SaveLmuWindowsVehicleClassBrakeWearSelectionUseCase,
    val observeText: ObserveLmuWindowsBrakeWearReadoutTextUseCase,
    val saveText: SaveLmuWindowsBrakeWearReadoutTextUseCase,
)

internal data class BrakeWearReadoutUseCases(
    val playSpeechEvent: PlaySpeechEventUseCase,
    val stopSpeech: StopSpeechUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class LmuWindowsAutoReadoutBrakeWearDetailViewModel(
    private val brakeWearUseCases: BrakeWearUseCases,
    observeAutoReadoutEnabledStates: ObserveAutoReadoutEnabledStatesUseCase,
    private val saveAutoReadoutEnabledState: SaveAutoReadoutEnabledStateUseCase,
    readout: BrakeWearReadoutUseCases,
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

    private val settingsState =
        combine(
            brakeWearUseCases.observeVehicleClassLowThreshold(),
            brakeWearUseCases.observeVehicleClassSelection(),
            observeAutoReadoutEnabledStates(Simulator.LmuWindows.id),
            brakeWearUseCases.observeText(),
            previewHelper.textToSpeechAvailable,
        ) { vehicleClassLowThresholdPercent, selectedVehicleClass, enabledStates, text, available ->
            LmuWindowsAutoReadoutBrakeWearDetailUiState(
                vehicleClassLowThresholdPercent = vehicleClassLowThresholdPercent,
                selectedVehicleClass = selectedVehicleClass,
                readoutText = text,
                isTextToSpeechAvailable = available,
                enabled = enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.BrakeWear.WarningReadout),
            )
        }

    val uiState: StateFlow<LmuWindowsAutoReadoutBrakeWearDetailUiState> =
        settingsState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsAutoReadoutBrakeWearDetailUiState(),
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
                LmuWindowsBrakeWearLow(
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
            saveAutoReadoutEnabledState(
                Simulator.LmuWindows.id,
                LmuWindowsReadoutItemKey.BrakeWear.WarningReadout,
                enabled,
            )
        }
    }
}
