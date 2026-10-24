package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsBlueFlag
import kurou.kodriver.domain.engine.LmuWindowsFullCourseYellow
import kurou.kodriver.domain.engine.LmuWindowsRedFlag
import kurou.kodriver.domain.engine.LmuWindowsYellowFlag
import kurou.kodriver.domain.engine.ReadoutTextEvent
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.lmuwindows.model.PrimaryFlag
import kurou.kodriver.domain.lmuwindows.model.SectorFlagState
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

class DetermineLmuWindowsRaceFlagReadoutUseCase {
    fun determineRaceFlags(
        state: LmuWindowsNarratorState,
        raceFlags: LmuWindowsRaceFlagsData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val previous =
            state.previousRaceFlags ?: return LmuWindowsNarratorReadoutDecision(
                state = state.copy(previousRaceFlags = raceFlags),
                events = emptyList(),
            )
        if (!settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.Flag.Root)) {
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
    ): List<ReadoutTextEvent> =
        listOfNotNull(
            determineBlueFlagEvent(previous, raceFlags, settings),
            determineYellowFlagEvent(previous, raceFlags, settings),
            determineFullCourseYellowEvent(previous, raceFlags, settings),
            determineRedFlagEvent(previous, raceFlags, settings),
        )

    private fun determineYellowFlagEvent(
        previous: LmuWindowsRaceFlagsData,
        raceFlags: LmuWindowsRaceFlagsData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): ReadoutTextEvent? {
        if (!settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.Flag.SectorYellowFlag)) return null
        val newYellowSector =
            raceFlags.sectorFlags.indices.any { i ->
                raceFlags.sectorFlags[i] == SectorFlagState.YELLOW &&
                    previous.sectorFlags.getOrNull(i) != SectorFlagState.YELLOW
            }
        return if (newYellowSector) LmuWindowsYellowFlag() else null
    }

    private fun determineFullCourseYellowEvent(
        previous: LmuWindowsRaceFlagsData,
        raceFlags: LmuWindowsRaceFlagsData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): ReadoutTextEvent? =
        if (
            settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.Flag.FullCourseYellow) &&
            previous.gamePhase != SessionPhase.FULL_COURSE_YELLOW &&
            raceFlags.gamePhase == SessionPhase.FULL_COURSE_YELLOW
        ) {
            LmuWindowsFullCourseYellow()
        } else {
            null
        }

    private fun determineRedFlagEvent(
        previous: LmuWindowsRaceFlagsData,
        raceFlags: LmuWindowsRaceFlagsData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): ReadoutTextEvent? =
        if (
            settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.Flag.RedFlag) &&
            previous.gamePhase != SessionPhase.RED_FLAG &&
            raceFlags.gamePhase == SessionPhase.RED_FLAG
        ) {
            // レッドフラッグ本文は自由文字列を読み上げる。
            LmuWindowsRedFlag()
        } else {
            null
        }
}

private fun determineBlueFlagEvent(
    previous: LmuWindowsRaceFlagsData,
    raceFlags: LmuWindowsRaceFlagsData,
    settings: LmuWindowsNarratorReadoutSettings,
): ReadoutTextEvent? =
    if (
        settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.Flag.BlueFlag) &&
        previous.playerFlag != PrimaryFlag.BLUE &&
        raceFlags.playerFlag == PrimaryFlag.BLUE
    ) {
        LmuWindowsBlueFlag()
    } else {
        null
    }
