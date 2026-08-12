package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureHighThresholdUseCase

/**
 * GT7 タイヤ温度アナウンス詳細設定の ViewModel。
 *
 * 高温閾値（スライダー）は DataStore に永続化される。過熱警告の有効/無効は画面表示中のみ有効な
 * ローカル状態として保持する（永続化は別PRで対応する）。
 */
internal class Gt7Ps5ReadoutTyreTemperatureDetailViewModel(
    observeHighThreshold: ObserveGt7Ps5TyreTemperatureHighThresholdUseCase,
    private val saveHighThreshold: SaveGt7Ps5TyreTemperatureHighThresholdUseCase,
) : ViewModel() {
    private val _overheatWarningEnabled = MutableStateFlow(true)

    val uiState: StateFlow<Gt7Ps5ReadoutTyreTemperatureDetailUiState> =
        combine(_overheatWarningEnabled, observeHighThreshold()) { overheatWarningEnabled, highThresholdCelsius ->
            Gt7Ps5ReadoutTyreTemperatureDetailUiState(
                overheatWarningEnabled = overheatWarningEnabled,
                highThresholdCelsius = highThresholdCelsius,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Gt7Ps5ReadoutTyreTemperatureDetailUiState(),
        )

    fun onOverheatWarningEnabledChanged(enabled: Boolean) {
        _overheatWarningEnabled.update { enabled }
    }

    fun onHighThresholdChanged(celsius: Int) {
        viewModelScope.launch { saveHighThreshold(celsius) }
    }

    fun onHighThresholdReset() {
        onHighThresholdChanged(GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)
    }
}
