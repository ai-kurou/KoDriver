package kurou.kodriver.feature.lmuwindowsreadout.raindetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * 永続化は未実装（GUIのみ先行実装）。設定はViewModelのインメモリ状態にのみ保持し、
 * 画面を離れると破棄される。
 */
internal class LmuWindowsReadoutRainDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LmuWindowsReadoutRainDetailUiState())
    val uiState: StateFlow<LmuWindowsReadoutRainDetailUiState> = _uiState

    fun onStartReadoutEnabledChanged(enabled: Boolean) {
        _uiState.update { it.copy(startReadoutEnabled = enabled) }
    }
}
