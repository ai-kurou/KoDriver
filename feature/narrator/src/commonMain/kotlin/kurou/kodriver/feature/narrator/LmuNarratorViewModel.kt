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
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class LmuNarratorViewModel(
    observeProximityUseCase: ObserveProximityUseCase,
    observeRaceFlagsUseCase: ObserveRaceFlagsUseCase,
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
    private val proximityJob = observeProximityUseCase()
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

    @Suppress("UnusedPrivateProperty")
    private val flagJob = observeRaceFlagsUseCase()
        .scan(null as RaceFlagsData? to null as RaceFlagsData?) { acc, current ->
            acc.second to current
        }
        .drop(1)
        .onEach { (prev, current) ->
            if (current == null) return@onEach
            announceFlags(prev, current)
        }
        .launchIn(viewModelScope)

    private fun announceFlags(prev: RaceFlagsData?, current: RaceFlagsData) {
        if (enabledStates.value[ReadoutItemKey.BLUE_FLAG] != false) {
            if (prev?.playerFlag != PrimaryFlag.BLUE && current.playerFlag == PrimaryFlag.BLUE) {
                ttsEngine.speak("ブルーフラッグ")
            }
        }

        if (enabledStates.value[ReadoutItemKey.SECTOR_YELLOW_FLAG] != false) {
            val prevSectors = prev?.sectorFlags ?: emptyList()
            val newYellowSector = current.sectorFlags.indices.any { i ->
                current.sectorFlags[i] == SectorFlagState.YELLOW &&
                    prevSectors.getOrNull(i) != SectorFlagState.YELLOW
            }
            if (newYellowSector) {
                ttsEngine.speak("イエローフラッグ")
            }
        }

        if (enabledStates.value[ReadoutItemKey.FULL_COURSE_YELLOW] != false) {
            if (prev?.gamePhase != SessionPhase.FULL_COURSE_YELLOW &&
                current.gamePhase == SessionPhase.FULL_COURSE_YELLOW
            ) {
                ttsEngine.speak("フルコースイエロー")
            }
        }

        if (enabledStates.value[ReadoutItemKey.SESSION_STOPPED] != false) {
            if (prev?.gamePhase != SessionPhase.SESSION_STOPPED &&
                current.gamePhase == SessionPhase.SESSION_STOPPED
            ) {
                ttsEngine.speak("セッションストップ")
            }
        }
    }
}
