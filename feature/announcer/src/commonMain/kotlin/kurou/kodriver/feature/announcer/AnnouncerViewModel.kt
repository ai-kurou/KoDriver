package kurou.kodriver.feature.announcer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class AnnouncerViewModel(
    observeProximityUseCase: ObserveProximityUseCase,
    observeSelectedSimulatorUseCase: ObserveSelectedSimulatorUseCase,
    observeReadoutEnabledStatesUseCase: ObserveReadoutEnabledStatesUseCase,
    private val ttsEngine: TextToSpeechEngine,
) : ViewModel() {

    private val enabledStates = observeSelectedSimulatorUseCase()
        .flatMapLatest { simulator ->
            if (simulator == null) emptyFlow() else observeReadoutEnabledStatesUseCase(simulator)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    @Suppress("UnusedPrivateProperty")
    private val announcerJob = observeProximityUseCase()
        .scan(null as ProximityData? to null as ProximityData?) { acc, current ->
            acc.second to current
        }
        .drop(1)
        .onEach { (prev, current) ->
            if (current == null) return@onEach
            if (enabledStates.value[ReadoutItemKey.VEHICLE_APPROACH] == false) return@onEach

            val leftJustAppeared = prev?.isSideBySideLeft != true && current.isSideBySideLeft
            val rightJustAppeared = prev?.isSideBySideRight != true && current.isSideBySideRight

            when {
                leftJustAppeared && rightJustAppeared -> ttsEngine.speak("両側")
                leftJustAppeared -> ttsEngine.speak("左")
                rightJustAppeared -> ttsEngine.speak("右")
            }
        }
        .launchIn(viewModelScope)
}
