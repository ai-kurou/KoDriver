package kurou.kodriver.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.DisconnectLmuUseCase
import kurou.kodriver.domain.usecase.ObserveLmuUseCase

class LmuViewModel(
    private val observeLmu: ObserveLmuUseCase,
    private val disconnect: DisconnectLmuUseCase,
    private val ttsEngine: TtsEngine = TtsEngine {},
) : ViewModel() {

    private val _uiState = MutableStateFlow<LmuUiState>(LmuUiState.Connecting)
    val uiState: StateFlow<LmuUiState> = _uiState.asStateFlow()

    // 200km/h到達アナウンス用。195km/h未満に落ちるまで再発話しない
    private var speed200Announced = false

    internal fun startObserving() {
        viewModelScope.launch {
            observeLmu()
                .catch { e -> _uiState.value = LmuUiState.Error(e.message ?: "Unknown error") }
                .collect { data ->
                    _uiState.value = LmuUiState.Connected(data)
                    checkSpeedAnnouncement(data.vehicle.speedKmh)
                }
        }
    }

    private fun checkSpeedAnnouncement(speedKmh: Double) {
        if (!speed200Announced && speedKmh >= 200.0) {
            speed200Announced = true
            ttsEngine.speak("時速200kmに達しました")
        } else if (speed200Announced && speedKmh < 195.0) {
            speed200Announced = false
        }
    }

    fun reconnect() {
        _uiState.value = LmuUiState.Connecting
        startObserving()
    }

    override fun onCleared() {
        super.onCleared()
        // viewModelScope は onCleared() より先にキャンセルされるため独立スコープを使う
        CoroutineScope(SupervisorJob()).launch { disconnect() }
    }

}
