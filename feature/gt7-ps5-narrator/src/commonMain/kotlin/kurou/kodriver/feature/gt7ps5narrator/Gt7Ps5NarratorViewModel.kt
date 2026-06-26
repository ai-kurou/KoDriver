package kurou.kodriver.feature.gt7ps5narrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

data class MyBestLapUseCases(
    val observeGt7Ps5: ObserveGt7Ps5UseCase,
    val observeMyBestLapVoiceType: ObserveMyBestLapVoiceTypeUseCase,
)

data class ReadoutListUseCases(
    val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val observeReadoutOrder: ObserveReadoutOrderUseCase,
)

data class RemainingFuelLapsUseCases(
    val observeRemainingFuelLapsThreshold: ObserveGt7Ps5RemainingFuelLapsUseCase,
)

private data class FuelTrackingState(
    val raceStartFuel: Float?,
    val raceStartLap: Int?,
    val currentLap: Int,
    val currentGasLevel: Float,
    val isNewSession: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5NarratorViewModel(
    myBestLapUseCases: MyBestLapUseCases,
    readoutListUseCases: ReadoutListUseCases,
    remainingFuelLapsUseCases: RemainingFuelLapsUseCases,
    private val ttsEngine: TextToSpeechEngine,
) : ViewModel() {

    private val selectedSimulator = readoutListUseCases.observeSelectedSimulator()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val enabledStates = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutEnabledStates(simulator.id)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val readoutOrder = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutOrder(simulator.id)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val voiceType = myBestLapUseCases.observeMyBestLapVoiceType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, MyBestLapVoiceType.FORMAL)

    private val fuelThreshold = remainingFuelLapsUseCases.observeRemainingFuelLapsThreshold()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 3)

    private var personalBestMs: Int = Int.MAX_VALUE
    private var lastAnnouncedRemainingLaps: Int = -1

    private val gt7TelemetryFlow = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.Gt7Ps5) emptyFlow()
            else myBestLapUseCases.observeGt7Ps5()
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    @Suppress("UnusedPrivateProperty")
    private val myBestLapJob = gt7TelemetryFlow
        .map { it.bestLapTimeMs }
        .distinctUntilChanged()
        .scan(null as Int? to null as Int?) { acc, current -> acc.second to current }
        .drop(1)
        .onEach { (prev, current) ->
            if (prev == null) return@onEach
            if (current == null || current <= 0) return@onEach
            if (prev > 0 && current >= prev) return@onEach
            if (current >= personalBestMs) return@onEach
            if (enabledStates.value[ReadoutItemKey.MyBestLap] == false) return@onEach
            personalBestMs = current
            val event = when (voiceType.value) {
                MyBestLapVoiceType.FORMAL -> SpeechEvent.MyBestLapFormal
                MyBestLapVoiceType.CASUAL -> SpeechEvent.MyBestLapCasual
            }
            speakWithPriority(event)
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val remainingFuelLapsJob = gt7TelemetryFlow
        .distinctUntilChangedBy { it.lapCount }
        .scan(FuelTrackingState(null, null, -1, 0f, false)) { state, data ->
            when {
                data.lapCount < state.currentLap -> FuelTrackingState(
                    raceStartFuel = data.gasLevel,
                    raceStartLap = data.lapCount,
                    currentLap = data.lapCount,
                    currentGasLevel = data.gasLevel,
                    isNewSession = true,
                )
                state.raceStartFuel == null -> FuelTrackingState(
                    raceStartFuel = data.gasLevel,
                    raceStartLap = data.lapCount,
                    currentLap = data.lapCount,
                    currentGasLevel = data.gasLevel,
                    isNewSession = false,
                )
                else -> state.copy(
                    currentLap = data.lapCount,
                    currentGasLevel = data.gasLevel,
                    isNewSession = false,
                )
            }
        }
        .drop(1)
        .onEach { state ->
            if (state.isNewSession) lastAnnouncedRemainingLaps = -1
            val startFuel = state.raceStartFuel ?: return@onEach
            val startLap = state.raceStartLap ?: return@onEach
            val lapsCompleted = state.currentLap - startLap
            if (lapsCompleted <= 0) return@onEach
            val consumedFuel = startFuel - state.currentGasLevel
            if (consumedFuel <= 0f) return@onEach
            val avgConsumption = consumedFuel / lapsCompleted
            val remainingLapsFloor = (state.currentGasLevel / avgConsumption).toInt()
            val threshold = fuelThreshold.value
            if (remainingLapsFloor < 1 || remainingLapsFloor > threshold) return@onEach
            if (remainingLapsFloor == lastAnnouncedRemainingLaps) return@onEach
            if (enabledStates.value[ReadoutItemKey.RemainingFuelLaps] == false) return@onEach
            lastAnnouncedRemainingLaps = remainingLapsFloor
            speakWithPriority(SpeechEvent.RemainingFuelLapsWarning(remainingLapsFloor))
        }
        .launchIn(viewModelScope)

    private fun speakWithPriority(event: SpeechEvent) {
        val order = readoutOrder.value
        val currentKey = ttsEngine.currentReadoutItemKey
        if (currentKey != null) {
            val currentIndex = order.indexOf(currentKey).takeIf { it != -1 } ?: Int.MAX_VALUE
            val newIndex = order.indexOf(event.readoutItemKey).takeIf { it != -1 } ?: Int.MAX_VALUE
            if (newIndex >= currentIndex) return
            ttsEngine.stop()
        }
        ttsEngine.speak(event)
    }
}
