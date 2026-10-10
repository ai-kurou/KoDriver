package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.formatLmuWindowsTyreTemperatureReadoutText
import kurou.kodriver.domain.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class TyreTemperatureUseCases(
    val observeEnabledStates: ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase,
    val observeLowWarningPhases: ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase,
    val observeVehicleClassHighThreshold: ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase,
    val observeVehicleClassSelection: ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase,
    val saveEnabledState: SaveLmuWindowsTyreTemperatureEnabledStateUseCase,
    val saveLowWarningPhases: SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase,
    val saveVehicleClassHighThreshold: SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase,
    val saveVehicleClassSelection: SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase,
)

internal data class TyreTemperatureReadoutUseCases(
    val observeText: ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase,
    val saveText: SaveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase,
    val observeColdText: ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase,
    val saveColdText: SaveLmuWindowsTyreTemperatureColdReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class LmuWindowsReadoutTyreTemperatureDetailViewModel(
    private val tyreTemperatureUseCases: TyreTemperatureUseCases,
    private val readout: TyreTemperatureReadoutUseCases,
) : ViewModel() {
    private val preview =
        ReadoutTextPreviewHelper(
            viewModelScope,
            readout.checkTextToSpeechAvailable,
            readout.observeSoundVolume,
            readout.playStartSoundForKey,
            readout.speakText,
        )

    private val temperatureSettings =
        combine(
            tyreTemperatureUseCases.observeEnabledStates(),
            tyreTemperatureUseCases.observeLowWarningPhases(),
            tyreTemperatureUseCases.observeVehicleClassHighThreshold(),
            tyreTemperatureUseCases.observeVehicleClassSelection(),
        ) { states, lowWarningPhases, vehicleClassHighThresholdCelsius, selectedVehicleClass ->
            LmuWindowsReadoutTyreTemperatureDetailUiState(
                overheatWarningEnabled = states.getValue(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning),
                lowWarningEnabled = states.getValue(LmuWindowsReadoutItemKey.TyreTemperature.LowWarning),
                lowWarningPhases = lowWarningPhases,
                vehicleClassHighThresholdCelsius = vehicleClassHighThresholdCelsius.mapValues { it.value.value },
                selectedVehicleClass = selectedVehicleClass,
            )
        }

    val uiState: StateFlow<LmuWindowsReadoutTyreTemperatureDetailUiState> =
        combine(
            temperatureSettings,
            readout.observeText(),
            readout.observeColdText(),
            preview.textToSpeechAvailable,
        ) { settings, text, coldText, available ->
            settings.copy(overheatReadoutText = text, coldReadoutText = coldText, isTextToSpeechAvailable = available)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutTyreTemperatureDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onOverheatWarningEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            tyreTemperatureUseCases.saveEnabledState(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning, enabled)
        }
    }

    fun onOverheatReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveText(text) }
    }

    /**
     * 空白文言・TTS利用不可・音量ゼロでは再生しない。
     * [celsius] は画面に表示中の選択クラスの高温閾値。保存の反映待ちで旧値になるのを避けるため呼び出し側から受け取る。
     */
    fun onOverheatReadoutTextPreviewClicked(
        text: String,
        celsius: Int,
    ) {
        previewReadoutText(formatLmuWindowsTyreTemperatureReadoutText(text, celsius))
    }

    private fun previewReadoutText(text: String) {
        preview.onPreviewClicked(text, LmuWindowsReadoutItemKey.TyreTemperature.Root)
    }

    fun onLowWarningEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            tyreTemperatureUseCases.saveEnabledState(LmuWindowsReadoutItemKey.TyreTemperature.LowWarning, enabled)
        }
    }

    fun onColdReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveColdText(text) }
    }

    /** 入力中の低温文言を過熱文言と同じTTS設定で試聴する。 */
    fun onLowWarningPreviewClicked(text: String) {
        previewReadoutText(formatLmuWindowsTyreTemperatureReadoutText(text, COLD_PREVIEW_CELSIUS))
    }

    fun onLowWarningPhaseToggled(phase: SessionPhase) {
        val currentPhases = uiState.value.lowWarningPhases
        val updatedPhases = if (phase in currentPhases) currentPhases - phase else currentPhases + phase
        viewModelScope.launch { tyreTemperatureUseCases.saveLowWarningPhases(updatedPhases) }
    }

    fun onVehicleClassHighThresholdChanged(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Int,
    ) {
        viewModelScope.launch {
            tyreTemperatureUseCases.saveVehicleClassHighThreshold(vehicleClass, Celsius(celsius))
        }
    }

    fun onVehicleClassHighThresholdReset(vehicleClass: LmuWindowsVehicleClassData) {
        viewModelScope.launch {
            tyreTemperatureUseCases.saveVehicleClassHighThreshold(
                vehicleClass,
                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(vehicleClass),
            )
        }
    }

    fun onVehicleClassSelected(vehicleClass: LmuWindowsVehicleClassData) {
        viewModelScope.launch { tyreTemperatureUseCases.saveVehicleClassSelection(vehicleClass) }
    }
}

/** 低温文言の試聴専用の代表温度。実際の警告判定には使用しない。 */
private const val COLD_PREVIEW_CELSIUS = 60
