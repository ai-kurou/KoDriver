package kurou.kodriver.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.CheckTelemetryConnectionUseCase
import kurou.kodriver.domain.usecase.DisconnectTelemetryUseCase
import kurou.kodriver.domain.usecase.ObserveTelemetryUseCase

class TelemetryViewModel(
    private val observeTelemetry: ObserveTelemetryUseCase,
    private val checkConnection: CheckTelemetryConnectionUseCase,
    private val disconnect: DisconnectTelemetryUseCase,
    private val ttsEngine: TtsEngine = TtsEngine {},
) : ViewModel() {

    private val _uiState = MutableStateFlow<TelemetryUiState>(TelemetryUiState.Connecting)
    val uiState: StateFlow<TelemetryUiState> = _uiState.asStateFlow()

    // 200km/h到達アナウンス用。195km/h未満に落ちるまで再発話しない
    private var speed200Announced = false

    init {
        startObserving()
    }

    private fun startObserving() {
        viewModelScope.launch {
            observeTelemetry()
                .catch { e -> _uiState.value = TelemetryUiState.Error(e.message ?: "Unknown error") }
                .collect { data ->
                    _uiState.value = TelemetryUiState.Connected(data)
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
        _uiState.value = TelemetryUiState.Connecting
        startObserving()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { disconnect() }
    }
}
