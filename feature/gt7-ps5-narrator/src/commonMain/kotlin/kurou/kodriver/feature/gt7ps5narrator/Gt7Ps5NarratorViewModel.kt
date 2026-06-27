package kurou.kodriver.feature.gt7ps5narrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
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
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEnabledUseCase
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
    val observeRemainingFuelLapsEnabled: ObserveGt7Ps5RemainingFuelLapsEnabledUseCase,
)

private data class FuelTrackingState(
    val raceStartFuel: Float?,
    val raceStartLap: Int?,
    val currentLap: Int,
    val currentLapStartedAtMs: Long,
    val currentGasLevel: Float,
    val bestLapTimeMs: Int,
    val totalRefueled: Float,
    val isNewSession: Boolean,
    val observedAtMs: Long,
)

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5NarratorViewModel(
    myBestLapUseCases: MyBestLapUseCases,
    readoutListUseCases: ReadoutListUseCases,
    remainingFuelLapsUseCases: RemainingFuelLapsUseCases,
    private val ttsEngine: TextToSpeechEngine,
    private val currentTimeMs: () -> Long = { System.currentTimeMillis() },
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

    private val remainingFuelLapsEnabled = remainingFuelLapsUseCases.observeRemainingFuelLapsEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private var personalBestMs: Int = Int.MAX_VALUE
    private var lastAnnouncedRemainingLaps: Int = -1
    private var lastFuelEvaluationLap: Int = -1

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
        .scan(FuelTrackingState(null, null, -1, 0L, 0f, -1, 0f, false, 0L)) { state, data ->
            val now = currentTimeMs()
            when {
                data.lapCount < state.currentLap -> FuelTrackingState(
                    raceStartFuel = data.gasLevel,
                    raceStartLap = data.lapCount,
                    currentLap = data.lapCount,
                    currentLapStartedAtMs = now,
                    currentGasLevel = data.gasLevel,
                    bestLapTimeMs = data.bestLapTimeMs,
                    totalRefueled = 0f,
                    isNewSession = true,
                    observedAtMs = now,
                )
                state.raceStartFuel == null -> FuelTrackingState(
                    raceStartFuel = data.gasLevel,
                    raceStartLap = data.lapCount,
                    currentLap = data.lapCount,
                    currentLapStartedAtMs = now,
                    currentGasLevel = data.gasLevel,
                    bestLapTimeMs = data.bestLapTimeMs,
                    totalRefueled = 0f,
                    isNewSession = false,
                    observedAtMs = now,
                )
                else -> {
                    val refueled = (data.gasLevel - state.currentGasLevel).coerceAtLeast(0f)
                    val currentLapStartedAtMs = if (data.lapCount != state.currentLap) {
                        now
                    } else {
                        state.currentLapStartedAtMs
                    }
                    state.copy(
                        currentLap = data.lapCount,
                        currentLapStartedAtMs = currentLapStartedAtMs,
                        currentGasLevel = data.gasLevel,
                        bestLapTimeMs = data.bestLapTimeMs,
                        totalRefueled = state.totalRefueled + refueled,
                        isNewSession = false,
                        observedAtMs = now,
                    )
                }
            }
        }
        .drop(1)
        .onEach { state ->
            if (state.isNewSession) {
                lastAnnouncedRemainingLaps = -1
                lastFuelEvaluationLap = -1
            }
            if (state.currentLap == lastFuelEvaluationLap) return@onEach
            val bestLapTimeMs = state.bestLapTimeMs
            if (bestLapTimeMs <= 0) return@onEach
            val readoutTimingMs = (bestLapTimeMs - REMAINING_FUEL_LAPS_READOUT_BEFORE_BEST_LAP_MS).coerceAtLeast(0)
            val currentLapElapsedMs = state.observedAtMs - state.currentLapStartedAtMs
            if (currentLapElapsedMs < readoutTimingMs) return@onEach
            val startFuel = state.raceStartFuel ?: return@onEach
            val startLap = state.raceStartLap ?: return@onEach
            val lapsCompleted = state.currentLap - startLap
            if (lapsCompleted <= 0) return@onEach
            lastFuelEvaluationLap = state.currentLap
            val consumedFuel = startFuel + state.totalRefueled - state.currentGasLevel
            if (consumedFuel <= 0f) return@onEach
            val avgConsumption = consumedFuel / lapsCompleted
            val remainingLapsFloor = (state.currentGasLevel / avgConsumption).toInt()
            val threshold = fuelThreshold.value
            if (remainingLapsFloor < 1 || remainingLapsFloor > threshold) return@onEach
            if (remainingLapsFloor == lastAnnouncedRemainingLaps) return@onEach
            if (!remainingFuelLapsEnabled.value) return@onEach
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

    private companion object {
        const val REMAINING_FUEL_LAPS_READOUT_BEFORE_BEST_LAP_MS = 30_000
    }
}
