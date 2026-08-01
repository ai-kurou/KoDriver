package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType

/**
 * LMU 向け読み上げ判定の継続状態。
 *
 * 旗、車両接近、自己ベスト、温度・摩耗・ピットタイミングなどの重複読み上げを避けるため、
 * 前回までの入力と判定結果を保持する。
 */
data class LmuWindowsNarratorState(
    val vehicleApproachState: LmuWindowsVehicleApproachState = LmuWindowsVehicleApproachState(),
    val previousRaceFlags: LmuWindowsRaceFlagsData? = null,
    val previousVehicleDamage: LmuWindowsVehicleDamageData? = null,
    val personalBestMs: Long = Long.MAX_VALUE,
    val previousBestLapTimeMs: Long? = null,
    val tyreOverheating: Boolean = false,
    val tyreWearWarned: Boolean = false,
    val previousGamePhaseForTyreLowWarning: SessionPhase? = null,
    val remainingVirtualEnergyWarned: Boolean = false,
    val lastAnnouncedPitTimingVirtualEnergyLaps: Int = -1,
    val lastPitTimingVirtualEnergyEvaluationLap: Int = -1,
    val pitTimingVirtualEnergyTrackingState: LmuWindowsPitTimingTrackingState = LmuWindowsPitTimingTrackingState(),
    val lastAnnouncedPitTimingTyreWearLaps: Int = -1,
    val lastPitTimingTyreWearEvaluationLap: Int = -1,
    val pitTimingTyreWearTrackingState: LmuWindowsPitTimingTrackingState = LmuWindowsPitTimingTrackingState(),
)

/**
 * ピットタイミング（バーチャルエナジー・タイヤ摩耗の予想残り周回数）の推定に使う追跡状態。
 * 直近に完走した（給油・タイヤ交換なしの）ラップの消費量を、次回以降の推定基準として使う
 * 直近完走ラップ基準の方式。
 */
data class LmuWindowsPitTimingTrackingState(
    val session: Int? = null,
    val currentLap: Int = -1,
    val currentLapStartedAtMs: Long = 0L,
    val currentLapStartValue: Double = 0.0,
    val currentLapHasRefilled: Boolean = false,
    val currentValue: Double = 0.0,
    /** 直近に完走した（給油・タイヤ交換なしの）ラップの消費量。まだ存在しなければ null。 */
    val lastValidLapConsumption: Double? = null,
    val bestLapTimeMs: Long = -1L,
    val hasRefilled: Boolean = false,
    val isNewSession: Boolean = false,
    val observedAtMs: Long = 0L,
)

/** LMU の車両接近読み上げで、左右それぞれの接近継続状態を保持する。 */
data class LmuWindowsVehicleApproachState(
    val left: Map<Int, LmuWindowsApproachState> = emptyMap(),
    val right: Map<Int, LmuWindowsApproachState> = emptyMap(),
)

/** 1 台の周辺車両に対する接近開始時刻と読み上げ済み状態。 */
data class LmuWindowsApproachState(
    val startedAtMs: Long,
    val announced: Boolean,
    val sustainedAnnounced: Boolean = false,
)

private data class ApproachSideStatesResult(
    val states: Map<Int, LmuWindowsApproachState>,
    val announce: Boolean,
    val sustainedAnnounce: Boolean,
)

/** LMU 向け読み上げ判定で参照するユーザー設定と現在周回情報。 */
data class LmuWindowsNarratorReadoutSettings(
    val enabledStates: Map<ReadoutItemKey, Boolean>,
    val myBestLapVoiceType: MyBestLapVoiceType,
    val redFlagVoiceType: RedFlagVoiceType,
    val currentLap: Int,
    val skipFirstLap: Boolean,
    val vehicleApproachStartReadoutType: VehicleApproachStartReadoutType,
    val vehicleApproachSustainedApproachDurationSeconds: Int,
    val vehicleApproachSustainedReadoutType: VehicleApproachSustainedReadoutType,
    val tyreTemperatureHighThresholdCelsius: Int,
    val tyreTemperatureLowWarningPhases: Set<SessionPhase>,
    val tyreWearThresholdPercentage: Int,
    val remainingVirtualEnergyThresholdPercentage: Int,
    val pitTimingVirtualEnergyLapsThreshold: Int,
    val pitTimingTyreWearLapsThreshold: Int,
)

/** LMU 向け読み上げ判定の結果。次回へ渡す状態と、今回再生すべきイベントを含む。 */
data class LmuWindowsNarratorReadoutDecision(
    val state: LmuWindowsNarratorState,
    val events: List<SpeechEvent>,
)

/**
 * LMU の共有メモリ由来データから、今回読み上げるべき音声イベントを決定する UseCase。
 */
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
        vehicleApproach: LmuWindowsVehicleApproachData,
        settings: LmuWindowsNarratorReadoutSettings,
        observedAtMs: Long,
    ): LmuWindowsNarratorReadoutDecision {
        val sustainedThresholdMs = settings.vehicleApproachSustainedApproachDurationSeconds * MILLIS_PER_SECOND
        val previousApproachState = state.vehicleApproachState
        val left = computeApproachSideStates(
            vehicleIds = vehicleApproach.sideBySideLeftVehicleIds,
            previous = previousApproachState.left,
            observedAtMs = observedAtMs,
            sustainedThresholdMs = sustainedThresholdMs,
        )
        val right = computeApproachSideStates(
            vehicleIds = vehicleApproach.sideBySideRightVehicleIds,
            previous = previousApproachState.right,
            observedAtMs = observedAtMs,
            sustainedThresholdMs = sustainedThresholdMs,
        )
        val nextState = state.copy(
            vehicleApproachState = LmuWindowsVehicleApproachState(
                left = left.states,
                right = right.states,
            ),
        )
        val event = determineVehicleApproachEvent(left.announce, right.announce, settings)
        val sustainedEvent = determineVehicleApproachSustainedEvent(
            leftSustainedAnnounce = left.sustainedAnnounce,
            rightSustainedAnnounce = right.sustainedAnnounce,
            settings = settings,
        )
        return LmuWindowsNarratorReadoutDecision(
            state = nextState,
            events = listOfNotNull(event, sustainedEvent),
        )
    }

    private fun computeApproachSideStates(
        vehicleIds: Set<Int>,
        previous: Map<Int, LmuWindowsApproachState>,
        observedAtMs: Long,
        sustainedThresholdMs: Long,
    ): ApproachSideStatesResult {
        var announce = false
        var sustainedAnnounce = false
        val states = vehicleIds.associateWith { id ->
            val prev = previous[id]
            if (prev == null) {
                LmuWindowsApproachState(startedAtMs = observedAtMs, announced = false)
            } else {
                val elapsedMs = observedAtMs - prev.startedAtMs
                val shouldAnnounce = !prev.announced && elapsedMs >= APPROACH_DEBOUNCE_MS
                val shouldAnnounceSustained = !prev.sustainedAnnounced && elapsedMs >= sustainedThresholdMs
                if (shouldAnnounce) announce = true
                if (shouldAnnounceSustained) sustainedAnnounce = true
                prev.copy(
                    announced = prev.announced || shouldAnnounce,
                    sustainedAnnounced = prev.sustainedAnnounced || shouldAnnounceSustained,
                )
            }
        }
        return ApproachSideStatesResult(states, announce, sustainedAnnounce)
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

    fun determineTyreTemperatureOverheat(
        state: LmuWindowsNarratorState,
        data: LmuWindowsTyreCarcassTemperatureData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val hotThreshold = settings.tyreTemperatureHighThresholdCelsius.toDouble()
        val coolThreshold = hotThreshold - TYRE_OVERHEAT_HYSTERESIS_CELSIUS
        val anyHot = data.wheels.values.any { it >= hotThreshold }
        val allCool = data.wheels.values.all { it <= coolThreshold }
        val nextOverheating = when {
            anyHot -> true
            allCool -> false
            else -> state.tyreOverheating
        }
        val shouldAnnounce = !state.tyreOverheating && nextOverheating &&
            settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.TyreTemperature.Root) &&
            settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(tyreOverheating = nextOverheating),
            events = if (shouldAnnounce) listOf(SpeechEvent.TyreOverheat) else emptyList(),
        )
    }

    fun determineTyreTemperatureLow(
        state: LmuWindowsNarratorState,
        data: LmuWindowsTyreCarcassTemperatureData,
        raceFlags: LmuWindowsRaceFlagsData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val previousGamePhase = state.previousGamePhaseForTyreLowWarning
        val enteringTargetPhase = previousGamePhase != null &&
            raceFlags.gamePhase != previousGamePhase &&
            raceFlags.gamePhase in settings.tyreTemperatureLowWarningPhases
        val anyCold = data.wheels.values.any { it <= TYRE_LOW_WARNING_THRESHOLD_CELSIUS }
        val shouldAnnounce = enteringTargetPhase && anyCold &&
            settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.TyreTemperature.Root) &&
            settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(previousGamePhaseForTyreLowWarning = raceFlags.gamePhase),
            events = if (shouldAnnounce) listOf(SpeechEvent.TyreCold) else emptyList(),
        )
    }

    fun determineTyreWear(
        state: LmuWindowsNarratorState,
        data: LmuWindowsTyreWearData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val anyWorn = data.wheels.values.any { remainingRatio ->
            (1.0 - remainingRatio) * PERCENTAGE_SCALE >= settings.tyreWearThresholdPercentage
        }
        val shouldAnnounce = !state.tyreWearWarned && anyWorn &&
            settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.TyreWear.Root)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(tyreWearWarned = anyWorn),
            events = if (shouldAnnounce) listOf(SpeechEvent.TyreWearWarning) else emptyList(),
        )
    }

    fun determineRemainingVirtualEnergy(
        state: LmuWindowsNarratorState,
        data: LmuWindowsVirtualEnergyData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val isLow = data.remainingRatio * PERCENTAGE_SCALE <= settings.remainingVirtualEnergyThresholdPercentage
        val shouldAnnounce = !state.remainingVirtualEnergyWarned && isLow &&
            settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(remainingVirtualEnergyWarned = isLow),
            events = if (shouldAnnounce) listOf(SpeechEvent.RemainingVirtualEnergyWarning) else emptyList(),
        )
    }

    fun determinePitTimingVirtualEnergy(
        state: LmuWindowsNarratorState,
        telemetry: LmuWindowsTelemetryData,
        virtualEnergy: LmuWindowsVirtualEnergyData,
        settings: LmuWindowsNarratorReadoutSettings,
        observedAtMs: Long,
    ): LmuWindowsNarratorReadoutDecision {
        val trackingState = trackPitTimingValue(
            state = state.pitTimingVirtualEnergyTrackingState,
            currentLap = telemetry.timing.currentLap,
            bestLapTimeMs = telemetry.timing.bestLapTimeMs,
            currentValue = virtualEnergy.remainingRatio,
            session = virtualEnergy.session,
            observedAtMs = observedAtMs,
        )
        val stateAfterTracking = when {
            trackingState.isNewSession -> state.copy(
                lastAnnouncedPitTimingVirtualEnergyLaps = -1,
                lastPitTimingVirtualEnergyEvaluationLap = -1,
                pitTimingVirtualEnergyTrackingState = trackingState,
            )

            trackingState.hasRefilled -> state.copy(
                lastAnnouncedPitTimingVirtualEnergyLaps = -1,
                pitTimingVirtualEnergyTrackingState = trackingState,
            )

            else -> state.copy(pitTimingVirtualEnergyTrackingState = trackingState)
        }
        val evaluation = calculatePitTimingRemainingLaps(
            trackingState = trackingState,
            lastEvaluationLap = stateAfterTracking.lastPitTimingVirtualEnergyEvaluationLap,
            lastAnnouncedLaps = stateAfterTracking.lastAnnouncedPitTimingVirtualEnergyLaps,
            threshold = settings.pitTimingVirtualEnergyLapsThreshold,
            enabled = settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.PitTiming.Root),
        )
        val stateAfterEvaluation =
            stateAfterTracking.copy(lastPitTimingVirtualEnergyEvaluationLap = evaluation.evaluatedLap)
        val remainingLaps = evaluation.remainingLaps ?: return LmuWindowsNarratorReadoutDecision(
            stateAfterEvaluation,
            emptyList(),
        )
        return LmuWindowsNarratorReadoutDecision(
            state = stateAfterEvaluation.copy(lastAnnouncedPitTimingVirtualEnergyLaps = remainingLaps),
            events = listOf(SpeechEvent.PitTimingWarning(remainingLaps)),
        )
    }

    fun determinePitTimingTyreWear(
        state: LmuWindowsNarratorState,
        telemetry: LmuWindowsTelemetryData,
        tyreWear: LmuWindowsTyreWearData,
        settings: LmuWindowsNarratorReadoutSettings,
        observedAtMs: Long,
    ): LmuWindowsNarratorReadoutDecision {
        val worstRemainingRatio = tyreWear.wheels.values.minOrNull()
            ?: return LmuWindowsNarratorReadoutDecision(state, emptyList())
        val trackingState = trackPitTimingValue(
            state = state.pitTimingTyreWearTrackingState,
            currentLap = telemetry.timing.currentLap,
            bestLapTimeMs = telemetry.timing.bestLapTimeMs,
            currentValue = worstRemainingRatio,
            session = null,
            observedAtMs = observedAtMs,
        )
        val stateAfterTracking = when {
            trackingState.isNewSession -> state.copy(
                lastAnnouncedPitTimingTyreWearLaps = -1,
                lastPitTimingTyreWearEvaluationLap = -1,
                pitTimingTyreWearTrackingState = trackingState,
            )

            trackingState.hasRefilled -> state.copy(
                lastAnnouncedPitTimingTyreWearLaps = -1,
                pitTimingTyreWearTrackingState = trackingState,
            )

            else -> state.copy(pitTimingTyreWearTrackingState = trackingState)
        }
        val evaluation = calculatePitTimingRemainingLaps(
            trackingState = trackingState,
            lastEvaluationLap = stateAfterTracking.lastPitTimingTyreWearEvaluationLap,
            lastAnnouncedLaps = stateAfterTracking.lastAnnouncedPitTimingTyreWearLaps,
            threshold = settings.pitTimingTyreWearLapsThreshold,
            enabled = settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.PitTiming.Root),
        )
        val stateAfterEvaluation =
            stateAfterTracking.copy(lastPitTimingTyreWearEvaluationLap = evaluation.evaluatedLap)
        val remainingLaps = evaluation.remainingLaps ?: return LmuWindowsNarratorReadoutDecision(
            stateAfterEvaluation,
            emptyList(),
        )
        return LmuWindowsNarratorReadoutDecision(
            state = stateAfterEvaluation.copy(lastAnnouncedPitTimingTyreWearLaps = remainingLaps),
            events = listOf(SpeechEvent.PitTimingWarning(remainingLaps)),
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
            when (settings.redFlagVoiceType) {
                RedFlagVoiceType.RED_FLAG -> SpeechEvent.RedFlag
                RedFlagVoiceType.SESSION_STOP -> SpeechEvent.SessionStop
            }
        } else {
            null
        }

    private fun determineVehicleApproachEvent(
        leftAnnounce: Boolean,
        rightAnnounce: Boolean,
        settings: LmuWindowsNarratorReadoutSettings,
    ): SpeechEvent? {
        if (!settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.VehicleApproach.Root)) return null
        if (!settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout)) return null
        // mLapNumber は 0 スタート（最初の計測周 = 0、フォーメーションラップは負値の可能性あり）
        if (settings.skipFirstLap && settings.currentLap <= 0) return null
        return when {
            leftAnnounce && !rightAnnounce -> ApproachSide.LEFT.toSpeechEvent(settings.vehicleApproachStartReadoutType)
            rightAnnounce && !leftAnnounce -> ApproachSide.RIGHT.toSpeechEvent(settings.vehicleApproachStartReadoutType)
            else -> null
        }
    }

    private fun determineVehicleApproachSustainedEvent(
        leftSustainedAnnounce: Boolean,
        rightSustainedAnnounce: Boolean,
        settings: LmuWindowsNarratorReadoutSettings,
    ): SpeechEvent? {
        if (!settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.VehicleApproach.Root)) return null
        if (!settings.enabledStates.getValue(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained)) return null
        if (settings.skipFirstLap && settings.currentLap <= 0) return null
        return when {
            leftSustainedAnnounce && !rightSustainedAnnounce -> {
                ApproachSide.LEFT.toSustainedSpeechEvent(settings.vehicleApproachSustainedReadoutType)
            }

            rightSustainedAnnounce && !leftSustainedAnnounce -> {
                ApproachSide.RIGHT.toSustainedSpeechEvent(settings.vehicleApproachSustainedReadoutType)
            }

            else -> {
                null
            }
        }
    }

    private companion object {
        const val APPROACH_DEBOUNCE_MS = 50L
        const val MILLIS_PER_SECOND = 1_000L
        const val TYRE_LOW_WARNING_THRESHOLD_CELSIUS = 60.0
        const val TYRE_OVERHEAT_HYSTERESIS_CELSIUS = 5.0
        const val PERCENTAGE_SCALE = 100.0
    }
}

private const val PIT_TIMING_READOUT_BEFORE_BEST_LAP_MS = 30_000L

/** これ未満の残量増加はジッタとみなし、給油・タイヤ交換として扱わない（割合 0.0〜1.0 に対する値）。 */
private const val PIT_TIMING_REFILL_DETECTION_MIN_RATIO = 0.005

private data class PitTimingRemainingLapsEvaluation(
    val evaluatedLap: Int,
    val remainingLaps: Int?,
)

/**
 * ピットタイミング（バーチャルエナジー・タイヤ摩耗）共通の追跡状態更新。
 * 直近に完走した（給油・タイヤ交換なしの）ラップの消費量を、次回以降の推定基準として使う。
 */
private fun trackPitTimingValue(
    state: LmuWindowsPitTimingTrackingState,
    currentLap: Int,
    bestLapTimeMs: Long,
    currentValue: Double,
    session: Int?,
    observedAtMs: Long,
): LmuWindowsPitTimingTrackingState =
    when {
        state.currentLap == -1 -> {
            LmuWindowsPitTimingTrackingState(
            session = session,
            currentLap = currentLap,
            currentLapStartedAtMs = observedAtMs,
            currentLapStartValue = currentValue,
            currentLapHasRefilled = false,
            currentValue = currentValue,
            lastValidLapConsumption = null,
            bestLapTimeMs = bestLapTimeMs,
            hasRefilled = false,
            isNewSession = false,
            observedAtMs = observedAtMs,
        )
        }

        (session != null && session != state.session) || currentLap < state.currentLap -> {
            LmuWindowsPitTimingTrackingState(
                session = session,
                currentLap = currentLap,
                currentLapStartedAtMs = observedAtMs,
                currentLapStartValue = currentValue,
                currentLapHasRefilled = false,
                currentValue = currentValue,
                lastValidLapConsumption = null,
                bestLapTimeMs = bestLapTimeMs,
                hasRefilled = false,
                isNewSession = true,
                observedAtMs = observedAtMs,
            )
        }

        else -> {
            // 共有メモリの値は微小な上振れ（ジッタ・torn read）を含みうるため、
            // しきい値未満の増加は給油・タイヤ交換とみなさず消費量の推定から除外する。
            val delta = currentValue - state.currentValue
            val refilled = if (delta >= PIT_TIMING_REFILL_DETECTION_MIN_RATIO) delta else 0.0
            if (currentLap != state.currentLap) {
                // ラップが変わるタイミングで、直前のラップが給油・タイヤ交換なしで完走していれば
                // その消費量を今後の残り周回数推定の基準として採用する。
                val completedLapConsumption = state.currentLapStartValue - state.currentValue
                val lastValidLapConsumption = if (!state.currentLapHasRefilled && completedLapConsumption > 0.0) {
                    completedLapConsumption
                } else {
                    state.lastValidLapConsumption
                }
                state.copy(
                    session = session,
                    currentLap = currentLap,
                    currentLapStartedAtMs = observedAtMs,
                    currentLapStartValue = currentValue,
                    currentLapHasRefilled = false,
                    currentValue = currentValue,
                    lastValidLapConsumption = lastValidLapConsumption,
                    bestLapTimeMs = bestLapTimeMs,
                    hasRefilled = refilled > 0.0,
                    isNewSession = false,
                    observedAtMs = observedAtMs,
                )
            } else {
                state.copy(
                    session = session,
                    currentLapHasRefilled = state.currentLapHasRefilled || refilled > 0.0,
                    currentValue = currentValue,
                    bestLapTimeMs = bestLapTimeMs,
                    hasRefilled = refilled > 0.0,
                    isNewSession = false,
                    observedAtMs = observedAtMs,
                )
            }
        }
    }

private fun calculatePitTimingRemainingLaps(
    trackingState: LmuWindowsPitTimingTrackingState,
    lastEvaluationLap: Int,
    lastAnnouncedLaps: Int,
    threshold: Int,
    enabled: Boolean,
): PitTimingRemainingLapsEvaluation {
    if (trackingState.currentLap == lastEvaluationLap) {
        return PitTimingRemainingLapsEvaluation(lastEvaluationLap, null)
    }
    val bestLapTimeMs = trackingState.bestLapTimeMs
    if (bestLapTimeMs <= 0L) return PitTimingRemainingLapsEvaluation(lastEvaluationLap, null)
    val readoutTimingMs = (bestLapTimeMs - PIT_TIMING_READOUT_BEFORE_BEST_LAP_MS).coerceAtLeast(0L)
    val currentLapElapsedMs = trackingState.observedAtMs - trackingState.currentLapStartedAtMs
    if (currentLapElapsedMs < readoutTimingMs) return PitTimingRemainingLapsEvaluation(lastEvaluationLap, null)
    val avgConsumption = trackingState.lastValidLapConsumption
        ?: return PitTimingRemainingLapsEvaluation(lastEvaluationLap, null)
    if (avgConsumption <= 0.0) return PitTimingRemainingLapsEvaluation(lastEvaluationLap, null)
    val remainingLapsFloor = (trackingState.currentValue / avgConsumption).toInt()
    if (remainingLapsFloor < 0 || remainingLapsFloor > threshold) {
        return PitTimingRemainingLapsEvaluation(lastEvaluationLap, null)
    }
    if (remainingLapsFloor == lastAnnouncedLaps) return PitTimingRemainingLapsEvaluation(lastEvaluationLap, null)
    if (!enabled) return PitTimingRemainingLapsEvaluation(lastEvaluationLap, null)
    return PitTimingRemainingLapsEvaluation(trackingState.currentLap, remainingLapsFloor)
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

    fun toSustainedSpeechEvent(readoutType: VehicleApproachSustainedReadoutType): SpeechEvent =
        when (this) {
            LEFT -> when (readoutType) {
                VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT -> SpeechEvent.KeepRight
                VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED -> SpeechEvent.RightSustained
            }

            RIGHT -> when (readoutType) {
                VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT -> SpeechEvent.KeepLeft
                VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED -> SpeechEvent.LeftSustained
            }
        }
}
