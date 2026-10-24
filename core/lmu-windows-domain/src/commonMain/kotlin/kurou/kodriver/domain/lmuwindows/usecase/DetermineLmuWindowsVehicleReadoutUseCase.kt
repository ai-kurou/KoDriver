package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsCarLeft
import kurou.kodriver.domain.engine.LmuWindowsCarLeftSustained
import kurou.kodriver.domain.engine.LmuWindowsCarRight
import kurou.kodriver.domain.engine.LmuWindowsCarRightSustained
import kurou.kodriver.domain.engine.LmuWindowsOverheating
import kurou.kodriver.domain.engine.LmuWindowsPartDetached
import kurou.kodriver.domain.engine.LmuWindowsTyreDetached
import kurou.kodriver.domain.engine.ReadoutTextEvent
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

private const val APPROACH_DEBOUNCE_MS = 50L
private const val MILLIS_PER_SECOND = 1_000L

class DetermineLmuWindowsVehicleReadoutUseCase {
    fun determineVehicleApproach(
        state: LmuWindowsNarratorState,
        vehicleApproach: LmuWindowsVehicleApproachData,
        settings: LmuWindowsNarratorReadoutSettings,
        observedAtMs: Long,
    ): LmuWindowsNarratorReadoutDecision {
        val sustainedThresholdMs = settings.vehicleApproachSustainedApproachDurationSeconds * MILLIS_PER_SECOND
        val previousApproachState = state.vehicleApproachState
        val left =
            computeApproachSideStates(
                vehicleIds = vehicleApproach.sideBySideLeftVehicleIds,
                previous = previousApproachState.left,
                observedAtMs = observedAtMs,
                sustainedThresholdMs = sustainedThresholdMs,
            )
        val right =
            computeApproachSideStates(
                vehicleIds = vehicleApproach.sideBySideRightVehicleIds,
                previous = previousApproachState.right,
                observedAtMs = observedAtMs,
                sustainedThresholdMs = sustainedThresholdMs,
            )
        val nextState =
            state.copy(
                vehicleApproachState =
                    LmuWindowsVehicleApproachState(
                        left = left.states,
                        right = right.states,
                    ),
            )
        val event = determineVehicleApproachEvent(left.announce, right.announce, settings)
        val sustainedEvent =
            determineVehicleApproachSustainedEvent(
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
        // 現在並走している車両だけを残す。離れた車両の履歴を破棄し、再接近時は待機時間から計り直す。
        val states =
            vehicleIds.associateWith { id ->
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
        val previous =
            state.previousVehicleDamage ?: return LmuWindowsNarratorReadoutDecision(
                state = state.copy(previousVehicleDamage = vehicleDamage),
                events = emptyList(),
            )
        val overheatEvent =
            if (
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.VehicleDamage.Root) &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.VehicleDamage.Overheat) &&
                !previous.overheating &&
                vehicleDamage.overheating
            ) {
                LmuWindowsOverheating()
            } else {
                null
            }
        val partDetachedEvent =
            if (
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.VehicleDamage.Root) &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.VehicleDamage.PartDetached) &&
                !previous.partDetached &&
                vehicleDamage.partDetached
            ) {
                LmuWindowsPartDetached()
            } else {
                null
            }
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(previousVehicleDamage = vehicleDamage),
            events = listOfNotNull(overheatEvent, partDetachedEvent),
        )
    }

    fun determineTyreDetached(
        state: LmuWindowsNarratorState,
        tyreDetached: LmuWindowsTyreDetachedData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val previous =
            state.previousTyreDetached ?: return LmuWindowsNarratorReadoutDecision(
                state = state.copy(previousTyreDetached = tyreDetached),
                events = emptyList(),
            )
        val newlyDetached =
            tyreDetached.wheels.any { (wheel, detached) -> detached && previous.wheels[wheel] != true }
        val event =
            if (
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.VehicleDamage.Root) &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached) &&
                newlyDetached
            ) {
                LmuWindowsTyreDetached()
            } else {
                null
            }
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(previousTyreDetached = tyreDetached),
            events = listOfNotNull(event),
        )
    }

    private fun determineVehicleApproachEvent(
        leftAnnounce: Boolean,
        rightAnnounce: Boolean,
        settings: LmuWindowsNarratorReadoutSettings,
    ): ReadoutTextEvent? {
        if (!settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.VehicleApproach.Root)) return null
        if (!settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.VehicleApproach.StartReadout)) return null
        // mLapNumber は 0 スタート（最初の計測周 = 0、フォーメーションラップは負値の可能性あり）
        if (settings.skipFirstLap && settings.currentLap <= 0) return null
        return when {
            leftAnnounce && !rightAnnounce -> ApproachSide.LEFT.toSpeechEvent()
            rightAnnounce && !leftAnnounce -> ApproachSide.RIGHT.toSpeechEvent()
            else -> null
        }
    }

    private fun determineVehicleApproachSustainedEvent(
        leftSustainedAnnounce: Boolean,
        rightSustainedAnnounce: Boolean,
        settings: LmuWindowsNarratorReadoutSettings,
    ): ReadoutTextEvent? {
        if (!settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.VehicleApproach.Root)) return null
        if (!settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.VehicleApproach.Sustained)) return null
        if (settings.skipFirstLap && settings.currentLap <= 0) return null
        return when {
            leftSustainedAnnounce && !rightSustainedAnnounce -> {
                ApproachSide.LEFT.toSustainedSpeechEvent()
            }

            rightSustainedAnnounce && !leftSustainedAnnounce -> {
                ApproachSide.RIGHT.toSustainedSpeechEvent()
            }

            else -> {
                null
            }
        }
    }
}

private enum class ApproachSide {
    LEFT,
    RIGHT,
    ;

    fun toSpeechEvent(): ReadoutTextEvent =
        when (this) {
            LEFT -> LmuWindowsCarLeft()
            RIGHT -> LmuWindowsCarRight()
        }

    fun toSustainedSpeechEvent(): ReadoutTextEvent =
        when (this) {
            LEFT -> LmuWindowsCarLeftSustained()
            RIGHT -> LmuWindowsCarRightSustained()
        }
}
