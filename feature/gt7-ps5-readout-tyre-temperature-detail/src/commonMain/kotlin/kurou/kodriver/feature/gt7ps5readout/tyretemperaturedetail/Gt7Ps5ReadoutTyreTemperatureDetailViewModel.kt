package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT

/**
 * GT7 タイヤ温度アナウンス詳細設定の ViewModel。
 *
 * 現時点では設定値の永続化（DataStore への保存・読み出し）は未実装のため、画面表示中のみ有効な
 * ローカル状態として保持する。永続化は別PRで対応する。
 */
internal class Gt7Ps5ReadoutTyreTemperatureDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(Gt7Ps5ReadoutTyreTemperatureDetailUiState())
    val uiState: StateFlow<Gt7Ps5ReadoutTyreTemperatureDetailUiState> = _uiState.asStateFlow()

    fun onOverheatWarningEnabledChanged(enabled: Boolean) {
        _uiState.update { it.copy(overheatWarningEnabled = enabled) }
    }

    fun onHighThresholdChanged(celsius: Int) {
        _uiState.update { it.copy(highThresholdCelsius = celsius) }
    }

    fun onHighThresholdReset() {
        onHighThresholdChanged(GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)
    }
}
