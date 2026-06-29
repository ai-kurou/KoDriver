package kurou.kodriver.feature.gt7ps5narrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.DetermineGt7Ps5NarratorReadoutUseCase
import kurou.kodriver.domain.usecase.Gt7Ps5NarratorReadoutSettings
import kurou.kodriver.domain.usecase.Gt7Ps5NarratorState
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

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5NarratorViewModel(
    myBestLapUseCases: MyBestLapUseCases,
    readoutListUseCases: ReadoutListUseCases,
    remainingFuelLapsUseCases: RemainingFuelLapsUseCases,
    private val ttsEngine: TextToSpeechEngine,
    private val determineGt7Ps5NarratorReadout: DetermineGt7Ps5NarratorReadoutUseCase =
        DetermineGt7Ps5NarratorReadoutUseCase(),
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

    private var narratorState = Gt7Ps5NarratorState()

    private val gt7TelemetryFlow = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.Gt7Ps5) emptyFlow()
            else myBestLapUseCases.observeGt7Ps5()
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    @Suppress("UnusedPrivateProperty")
    private val narratorJob = gt7TelemetryFlow
        .onEach { telemetry ->
            val decision = determineGt7Ps5NarratorReadout(
                state = narratorState,
                telemetry = telemetry,
                settings = Gt7Ps5NarratorReadoutSettings(
                    enabledStates = enabledStates.value,
                    myBestLapVoiceType = voiceType.value,
                    remainingFuelLapsThreshold = fuelThreshold.value,
                    remainingFuelLapsEnabled = remainingFuelLapsEnabled.value,
                ),
                observedAtMs = currentTimeMs(),
            )
            narratorState = decision.state
            decision.events.forEach(::speakWithPriority)
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
