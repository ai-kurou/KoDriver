package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureHighThresholdUseCase

/**
 * GT7 タイヤ温度アナウンス詳細設定の ViewModel。
 *
 * 高温閾値（スライダー）・過熱警告の有効/無効はいずれも DataStore に永続化される。
 */
internal class Gt7Ps5ReadoutTyreTemperatureDetailViewModel(
    observeEnabledStates: ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase,
    observeHighThreshold: ObserveGt7Ps5TyreTemperatureHighThresholdUseCase,
    private val saveEnabledState: SaveGt7Ps5TyreTemperatureEnabledStateUseCase,
    private val saveHighThreshold: SaveGt7Ps5TyreTemperatureHighThresholdUseCase,
) : ViewModel() {
    val uiState: StateFlow<Gt7Ps5ReadoutTyreTemperatureDetailUiState> =
        combine(observeEnabledStates(), observeHighThreshold()) { states, highThresholdCelsius ->
            Gt7Ps5ReadoutTyreTemperatureDetailUiState(
                overheatWarningEnabled = states.getValue(ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning),
                highThresholdCelsius = highThresholdCelsius,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Gt7Ps5ReadoutTyreTemperatureDetailUiState(),
        )

    fun onOverheatWarningEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveEnabledState(ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning, enabled)
        }
    }

    fun onHighThresholdChanged(celsius: Int) {
        viewModelScope.launch { saveHighThreshold(celsius) }
    }

    fun onHighThresholdReset() {
        onHighThresholdChanged(GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)
    }
}
