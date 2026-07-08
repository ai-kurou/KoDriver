package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository

internal class FakeLmuWindowsFlagRepository(
    initialGamePhase: SessionPhase = SessionPhase.UNKNOWN,
) : LmuWindowsFlagRepository {
    private val _raceFlags = MutableStateFlow(defaultRaceFlagsData(initialGamePhase))

    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = _raceFlags.asStateFlow()

    fun updateGamePhase(gamePhase: SessionPhase) {
        _raceFlags.update { it.copy(gamePhase = gamePhase) }
    }

    private companion object {
        fun defaultRaceFlagsData(gamePhase: SessionPhase) = LmuWindowsRaceFlagsData(
            gamePhase = gamePhase,
            yellowFlagState = SessionYellowFlagState.NONE,
            sectorFlags = emptyList(),
            startLight = 0,
            numRedLights = 0,
            playerFlag = PrimaryFlag.GREEN,
            playerUnderYellow = false,
            playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
        )
    }
}
