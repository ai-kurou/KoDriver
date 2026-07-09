package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LmuWindowsProximityData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType

data class LmuWindowsNarratorState(
    val vehicleApproachState: LmuWindowsVehicleApproachState = LmuWindowsVehicleApproachState(),
    val previousRaceFlags: LmuWindowsRaceFlagsData? = null,
    val previousVehicleDamage: LmuWindowsVehicleDamageData? = null,
    val personalBestMs: Long = Long.MAX_VALUE,
    val previousBestLapTimeMs: Long? = null,
    val tyreOverheating: Boolean = false,
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
    val myBestLapVoiceType: MyBestLapVoiceType,
    val currentLap: Int,
    val skipFirstLap: Boolean,
    val vehicleApproachStartReadoutEnabled: Boolean,
    val vehicleApproachStartReadoutType: VehicleApproachStartReadoutType,
    val tyreTemperatureHighThresholdCelsius: Int,
)

data class LmuWindowsNarratorReadoutDecision(
    val state: LmuWindowsNarratorState,
    val events: List<SpeechEvent>,
)

class DetermineLmuWindowsNarratorReadoutUseCase {
    fun determineMyBestLap(
        state: LmuWindowsNarratorState,
        telemetry: LmuWindowsTelemetryData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val current = telemetry.timing.bestLapTimeMs
        val stateWithCurrentBestLap = state.copy(previousBestLapTimeMs = current)
        val previous = state.previousBestLapTimeMs
        if (previous == null) return LmuWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        if (current <= 0L) return LmuWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        if (previous > 0L && current >= previous) {
            return LmuWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }
        if (current >= state.personalBestMs) {
            return LmuWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }
        if (!settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.MyBestLap.Root)) {
            return LmuWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }

        val event = when (settings.myBestLapVoiceType) {
            MyBestLapVoiceType.FORMAL -> SpeechEvent.LmuWindowsMyBestLapFormal
            MyBestLapVoiceType.CASUAL -> SpeechEvent.LmuWindowsMyBestLapCasual
        }
        return LmuWindowsNarratorReadoutDecision(
            state = stateWithCurrentBestLap.copy(personalBestMs = current),
            events = listOf(event),
        )
    }

    fun determineVehicleApproach(
        state: LmuWindowsNarratorState,
        proximity: LmuWindowsProximityData,
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
        vehicleDamage: LmuWindowsVehicleDamageData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val previous = state.previousVehicleDamage ?: return LmuWindowsNarratorReadoutDecision(
            state = state.copy(previousVehicleDamage = vehicleDamage),
            events = emptyList(),
        )
        val event = if (
            settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.VehicleDamage.Root) &&
            settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat) &&
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

    fun determineTyreTemperature(
        state: LmuWindowsNarratorState,
        data: LmuWindowsTyreCarcassTemperatureData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val threshold = settings.tyreTemperatureHighThresholdCelsius.toDouble()
        val anyOverheating = data.wheels.values.any { it >= threshold }
        val shouldAnnounce = !state.tyreOverheating && anyOverheating &&
            settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.TyreTemperature.Root) &&
            settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(tyreOverheating = anyOverheating),
            events = if (shouldAnnounce) listOf(SpeechEvent.TyreOverheat) else emptyList(),
        )
    }

    fun determineRaceFlags(
        state: LmuWindowsNarratorState,
        raceFlags: LmuWindowsRaceFlagsData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val previous = state.previousRaceFlags ?: return LmuWindowsNarratorReadoutDecision(
            state = state.copy(previousRaceFlags = raceFlags),
            events = emptyList(),
        )
        if (!settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.Flag.Root)) {
            return LmuWindowsNarratorReadoutDecision(
                state = state.copy(previousRaceFlags = raceFlags),
                events = emptyList(),
            )
        }
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(previousRaceFlags = raceFlags),
            events = determineRaceFlagEvents(previous, raceFlags, settings),
        )
    }

    private fun determineRaceFlagEvents(
        previous: LmuWindowsRaceFlagsData,
        raceFlags: LmuWindowsRaceFlagsData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): List<SpeechEvent> = listOfNotNull(
        determineBlueFlagEvent(previous, raceFlags, settings),
        determineYellowFlagEvent(previous, raceFlags, settings),
        determineFullCourseYellowEvent(previous, raceFlags, settings),
        determineRedFlagEvent(previous, raceFlags, settings),
    )

    private fun determineBlueFlagEvent(
        previous: LmuWindowsRaceFlagsData,
        raceFlags: LmuWindowsRaceFlagsData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): SpeechEvent? =
        if (
            settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.Flag.BlueFlag) &&
            previous.playerFlag != PrimaryFlag.BLUE &&
            raceFlags.playerFlag == PrimaryFlag.BLUE
        ) {
            SpeechEvent.BlueFlag
        } else {
            null
        }

    private fun determineYellowFlagEvent(
        previous: LmuWindowsRaceFlagsData,
        raceFlags: LmuWindowsRaceFlagsData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): SpeechEvent? {
        if (!settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag)) return null
        val newYellowSector = raceFlags.sectorFlags.indices.any { i ->
            raceFlags.sectorFlags[i] == SectorFlagState.YELLOW &&
                previous.sectorFlags.getOrNull(i) != SectorFlagState.YELLOW
        }
        return if (newYellowSector) SpeechEvent.YellowFlag else null
    }

    private fun determineFullCourseYellowEvent(
        previous: LmuWindowsRaceFlagsData,
        raceFlags: LmuWindowsRaceFlagsData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): SpeechEvent? =
        if (
            settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.Flag.FullCourseYellow) &&
            previous.gamePhase != SessionPhase.FULL_COURSE_YELLOW &&
            raceFlags.gamePhase == SessionPhase.FULL_COURSE_YELLOW
        ) {
            SpeechEvent.FullCourseYellow
        } else {
            null
        }

    private fun determineRedFlagEvent(
        previous: LmuWindowsRaceFlagsData,
        raceFlags: LmuWindowsRaceFlagsData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): SpeechEvent? =
        if (
            settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.Flag.RedFlag) &&
            previous.gamePhase != SessionPhase.RED_FLAG &&
            raceFlags.gamePhase == SessionPhase.RED_FLAG
        ) {
            SpeechEvent.SessionStop
        } else {
            null
        }

    private fun determineVehicleApproachEvent(
        leftAnnounce: Boolean,
        rightAnnounce: Boolean,
        settings: LmuWindowsNarratorReadoutSettings,
    ): SpeechEvent? {
        if (!settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.VehicleApproach.Root)) return null
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
