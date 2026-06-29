package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleDamageData

data class LmuWindowsNarratorState(
    val vehicleApproachState: LmuWindowsVehicleApproachState = LmuWindowsVehicleApproachState(),
    val previousRaceFlags: RaceFlagsData? = null,
    val previousVehicleDamage: VehicleDamageData? = null,
)

data class LmuWindowsVehicleApproachState(
    val left: Map<Int, LmuWindowsApproachState> = emptyMap(),
    val right: Map<Int, LmuWindowsApproachState> = emptyMap(),
)

data class LmuWindowsApproachState(
    val startedAtMs: Long,
    val announced: Boolean,
)

data class LmuWindowsNarratorReadoutSettings(
    val enabledStates: Map<ReadoutItemKey, Boolean>,
    val currentLap: Int,
    val skipFirstLap: Boolean,
    val vehicleApproachStartReadoutEnabled: Boolean,
    val vehicleApproachStartReadoutType: VehicleApproachStartReadoutType,
)

data class LmuWindowsNarratorReadoutDecision(
    val state: LmuWindowsNarratorState,
    val events: List<SpeechEvent>,
)

class DetermineLmuWindowsNarratorReadoutUseCase {
    fun determineVehicleApproach(
        state: LmuWindowsNarratorState,
        proximity: ProximityData,
        settings: LmuWindowsNarratorReadoutSettings,
        observedAtMs: Long,
    ): LmuWindowsNarratorReadoutDecision {
        var leftAnnounce = false
        var rightAnnounce = false
        val previousApproachState = state.vehicleApproachState
        val newLeft = proximity.sideBySideLeftVehicleIds.associateWith { id ->
            val prev = previousApproachState.left[id]
            if (prev == null) {
                LmuWindowsApproachState(startedAtMs = observedAtMs, announced = false)
            } else {
                val shouldAnnounce = !prev.announced && observedAtMs - prev.startedAtMs >= APPROACH_DEBOUNCE_MS
                if (shouldAnnounce) leftAnnounce = true
                prev.copy(announced = prev.announced || shouldAnnounce)
            }
        }
        val newRight = proximity.sideBySideRightVehicleIds.associateWith { id ->
            val prev = previousApproachState.right[id]
            if (prev == null) {
                LmuWindowsApproachState(startedAtMs = observedAtMs, announced = false)
            } else {
                val shouldAnnounce = !prev.announced && observedAtMs - prev.startedAtMs >= APPROACH_DEBOUNCE_MS
                if (shouldAnnounce) rightAnnounce = true
                prev.copy(announced = prev.announced || shouldAnnounce)
            }
        }
        val nextState = state.copy(
            vehicleApproachState = LmuWindowsVehicleApproachState(
                left = newLeft,
                right = newRight,
            ),
        )
        val event = determineVehicleApproachEvent(leftAnnounce, rightAnnounce, settings)
        return LmuWindowsNarratorReadoutDecision(
            state = nextState,
            events = listOfNotNull(event),
        )
    }

    fun determineVehicleDamage(
        state: LmuWindowsNarratorState,
        vehicleDamage: VehicleDamageData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val previous = state.previousVehicleDamage ?: return LmuWindowsNarratorReadoutDecision(
            state = state.copy(previousVehicleDamage = vehicleDamage),
            events = emptyList(),
        )
        val event = if (
            settings.enabledStates[ReadoutItemKey.Overheat] != false &&
            !previous.overheating &&
            vehicleDamage.overheating
        ) {
            SpeechEvent.Overheating
        } else {
            null
        }
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(previousVehicleDamage = vehicleDamage),
            events = listOfNotNull(event),
        )
    }

    fun determineRaceFlags(
        state: LmuWindowsNarratorState,
        raceFlags: RaceFlagsData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val previous = state.previousRaceFlags ?: return LmuWindowsNarratorReadoutDecision(
            state = state.copy(previousRaceFlags = raceFlags),
            events = emptyList(),
        )
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(previousRaceFlags = raceFlags),
            events = buildList {
                if (
                    settings.enabledStates[ReadoutItemKey.BlueFlag] != false &&
                    previous.playerFlag != PrimaryFlag.BLUE &&
                    raceFlags.playerFlag == PrimaryFlag.BLUE
                ) {
                    add(SpeechEvent.BlueFlag)
                }
                if (settings.enabledStates[ReadoutItemKey.SectorYellowFlag] != false) {
                    val newYellowSector = raceFlags.sectorFlags.indices.any { i ->
                        raceFlags.sectorFlags[i] == SectorFlagState.YELLOW &&
                            previous.sectorFlags.getOrNull(i) != SectorFlagState.YELLOW
                    }
                    if (newYellowSector) add(SpeechEvent.YellowFlag)
                }
                if (
                    settings.enabledStates[ReadoutItemKey.FullCourseYellow] != false &&
                    previous.gamePhase != SessionPhase.FULL_COURSE_YELLOW &&
                    raceFlags.gamePhase == SessionPhase.FULL_COURSE_YELLOW
                ) {
                    add(SpeechEvent.FullCourseYellow)
                }
                if (
                    settings.enabledStates[ReadoutItemKey.RedFlag] != false &&
                    previous.gamePhase != SessionPhase.RED_FLAG &&
                    raceFlags.gamePhase == SessionPhase.RED_FLAG
                ) {
                    add(SpeechEvent.SessionStop)
                }
            },
        )
    }

    private fun determineVehicleApproachEvent(
        leftAnnounce: Boolean,
        rightAnnounce: Boolean,
        settings: LmuWindowsNarratorReadoutSettings,
    ): SpeechEvent? {
        if (settings.enabledStates[ReadoutItemKey.VehicleApproach] == false) return null
        if (!settings.vehicleApproachStartReadoutEnabled) return null
        // mLapNumber は 0 スタート（最初の計測周 = 0、フォーメーションラップは負値の可能性あり）
        if (settings.skipFirstLap && settings.currentLap <= 0) return null
        return when {
            leftAnnounce && !rightAnnounce -> ApproachSide.LEFT.toSpeechEvent(settings.vehicleApproachStartReadoutType)
            rightAnnounce && !leftAnnounce -> ApproachSide.RIGHT.toSpeechEvent(settings.vehicleApproachStartReadoutType)
            else -> null
        }
    }

    private companion object {
        const val APPROACH_DEBOUNCE_MS = 50L
    }
}

private enum class ApproachSide {
    LEFT,
    RIGHT,
    ;

    fun toSpeechEvent(readoutType: VehicleApproachStartReadoutType): SpeechEvent =
        when (this) {
            LEFT -> when (readoutType) {
                VehicleApproachStartReadoutType.CAR_LEFT_RIGHT -> SpeechEvent.CarLeft
                VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH -> SpeechEvent.LeftApproach
            }
            RIGHT -> when (readoutType) {
                VehicleApproachStartReadoutType.CAR_LEFT_RIGHT -> SpeechEvent.CarRight
                VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH -> SpeechEvent.RightApproach
            }
        }
}
