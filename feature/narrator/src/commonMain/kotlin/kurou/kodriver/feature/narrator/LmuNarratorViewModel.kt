package kurou.kodriver.feature.narrator

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
class LmuNarratorViewModel(
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
    private val narratorJob = observeProximityUseCase()
        .scan(null as ProximityData? to null as ProximityData?) { acc, current ->
            acc.second to current
        }
        .drop(1)
        .onEach { (prev, current) ->
            if (current == null) return@onEach
            if (enabledStates.value[ReadoutItemKey.VEHICLE_APPROACH] == false) return@onEach

            val prevLeftIds = prev?.sideBySideLeftVehicleIds ?: emptySet()
            val prevRightIds = prev?.sideBySideRightVehicleIds ?: emptySet()
            val newLeftVehicle = (current.sideBySideLeftVehicleIds - prevLeftIds).isNotEmpty()
            val newRightVehicle = (current.sideBySideRightVehicleIds - prevRightIds).isNotEmpty()

            when {
                // 両方同時の場合は読み上げないで一旦様子見る
                /*
                newLeftVehicle && newRightVehicle -> {
                    ttsEngine.speak("カーレフト")
                    ttsEngine.speak("カーライト")
                }
                 */
                newLeftVehicle -> ttsEngine.speak("カーレフト")
                newRightVehicle -> ttsEngine.speak("カーライト")
            }
        }
        .launchIn(viewModelScope)
}
