package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureHighThresholdUseCase

/**
 * ACE タイヤ温度アナウンス詳細設定の ViewModel。
 *
 * 高温閾値（スライダー）・過熱警告の有効/無効はいずれも DataStore に永続化される。
 */
internal data class TyreTemperatureUseCases(
    val observeEnabledStates: ObserveAceWindowsTyreTemperatureEnabledStatesUseCase,
    val observeHighThreshold: ObserveAceWindowsTyreTemperatureHighThresholdUseCase,
    val saveEnabledState: SaveAceWindowsTyreTemperatureEnabledStateUseCase,
    val saveHighThreshold: SaveAceWindowsTyreTemperatureHighThresholdUseCase,
)

internal class AceWindowsReadoutTyreTemperatureDetailViewModel(
    private val tyreTemperatureUseCases: TyreTemperatureUseCases,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<AceWindowsReadoutTyreTemperatureDetailUiState> =
        combine(
            tyreTemperatureUseCases.observeEnabledStates(),
            tyreTemperatureUseCases.observeHighThreshold(),
        ) { states, highThresholdCelsius ->
            AceWindowsReadoutTyreTemperatureDetailUiState(
                overheatWarningEnabled = states.getValue(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning),
                highThresholdCelsius = highThresholdCelsius.value,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AceWindowsReadoutTyreTemperatureDetailUiState(),
        )

    fun onOverheatWarningEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            tyreTemperatureUseCases.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, enabled)
        }
    }

    fun onHighThresholdChanged(celsius: Int) {
        viewModelScope.launch { tyreTemperatureUseCases.saveHighThreshold(Celsius(celsius)) }
    }

    fun onHighThresholdReset() {
        onHighThresholdChanged(ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value)
    }

    fun onPreviewClicked() {
        playSpeechEvent(
            SpeechEvent.AceWindowsTyreOverheat(ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value),
        )
    }
}
