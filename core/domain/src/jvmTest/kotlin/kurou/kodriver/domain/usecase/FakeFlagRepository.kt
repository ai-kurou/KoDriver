package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.repository.FlagRepository

internal class FakeFlagRepository(
    private val stream: Flow<RaceFlagsData> = flowOf(),
) : FlagRepository {
    override fun flagStream(): Flow<RaceFlagsData> = stream
}

internal fun fakeRaceFlagsData(
    gamePhase: Int = 0,
    yellowFlagState: Int = 0,
    playerFlag: Int = 0,
) = RaceFlagsData(
    gamePhase = gamePhase,
    yellowFlagState = yellowFlagState,
    sectorFlags = listOf(0, 0, 0),
    startLight = 0,
    numRedLights = 0,
    playerFlag = playerFlag,
    playerUnderYellow = false,
    playerCountLapFlag = 0,
)
