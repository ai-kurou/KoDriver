package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.repository.FlagRepository

internal class FakeFlagRepository(
    private val stream: Flow<RaceFlagsData> = flowOf(),
) : FlagRepository {
    override fun flagStream(): Flow<RaceFlagsData> = stream
}

internal fun fakeRaceFlagsData(
    gamePhase: SessionPhase = SessionPhase.GARAGE,
    yellowFlagState: SessionYellowFlagState = SessionYellowFlagState.NONE,
    playerFlag: PrimaryFlag = PrimaryFlag.GREEN,
) = RaceFlagsData(
    gamePhase = gamePhase,
    yellowFlagState = yellowFlagState,
    sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
    startLight = 0,
    numRedLights = 0,
    playerFlag = playerFlag,
    playerUnderYellow = false,
    playerCountLapFlag = CountLapFlag.DO_NOT_COUNT_LAP_OR_TIME,
)
