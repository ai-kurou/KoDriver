package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository

internal class FakeLmuWindowsFlagRepository(
    private val stream: Flow<LmuWindowsRaceFlagsData> = flowOf(),
) : LmuWindowsFlagRepository {
    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = stream
}

internal fun fakeRaceFlagsData(
    gamePhase: SessionPhase = SessionPhase.GARAGE,
    yellowFlagState: SessionYellowFlagState = SessionYellowFlagState.NONE,
    playerFlag: PrimaryFlag = PrimaryFlag.GREEN,
) = LmuWindowsRaceFlagsData(
    gamePhase = gamePhase,
    yellowFlagState = yellowFlagState,
    sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
    startLight = 0,
    numRedLights = 0,
    playerFlag = playerFlag,
    playerUnderYellow = false,
    playerCountLapFlag = CountLapFlag.DO_NOT_COUNT_LAP_OR_TIME,
)
