package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import org.junit.Test
import kotlin.test.assertEquals

class DetermineActiveRaceFlagsTest {

    @Test
    fun `アクティブなフラグがない場合は空リストを返す`() {
        val raceFlags = sampleRaceFlags()

        assertEquals(emptyList(), determineActiveRaceFlags(raceFlags))
    }

    @Test
    fun `playerFlagがBLUEの場合はBLUEを含む`() {
        val raceFlags = sampleRaceFlags(playerFlag = PrimaryFlag.BLUE)

        assertEquals(listOf(ActiveRaceFlag.BLUE), determineActiveRaceFlags(raceFlags))
    }

    @Test
    fun `playerUnderYellowがtrueの場合はYELLOWを含む`() {
        val raceFlags = sampleRaceFlags(playerUnderYellow = true)

        assertEquals(listOf(ActiveRaceFlag.YELLOW), determineActiveRaceFlags(raceFlags))
    }

    @Test
    fun `sectorFlagsにYELLOWが含まれる場合はYELLOWを含む`() {
        val raceFlags = sampleRaceFlags(
            sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.YELLOW, SectorFlagState.CLEAR),
        )

        assertEquals(listOf(ActiveRaceFlag.YELLOW), determineActiveRaceFlags(raceFlags))
    }

    @Test
    fun `gamePhaseがFULL_COURSE_YELLOWの場合はFULL_COURSE_YELLOWを含む`() {
        val raceFlags = sampleRaceFlags(gamePhase = SessionPhase.FULL_COURSE_YELLOW)

        assertEquals(listOf(ActiveRaceFlag.FULL_COURSE_YELLOW), determineActiveRaceFlags(raceFlags))
    }

    @Test
    fun `gamePhaseがRED_FLAGの場合はREDを含む`() {
        val raceFlags = sampleRaceFlags(gamePhase = SessionPhase.RED_FLAG)

        assertEquals(listOf(ActiveRaceFlag.RED), determineActiveRaceFlags(raceFlags))
    }

    private fun sampleRaceFlags(
        gamePhase: SessionPhase = SessionPhase.GREEN_FLAG,
        sectorFlags: List<SectorFlagState> = listOf(
            SectorFlagState.CLEAR,
            SectorFlagState.CLEAR,
            SectorFlagState.CLEAR,
        ),
        playerFlag: PrimaryFlag = PrimaryFlag.GREEN,
        playerUnderYellow: Boolean = false,
    ) = LmuWindowsRaceFlagsData(
        gamePhase = gamePhase,
        yellowFlagState = SessionYellowFlagState.NONE,
        sectorFlags = sectorFlags,
        startLight = 0,
        numRedLights = 0,
        playerFlag = playerFlag,
        playerUnderYellow = playerUnderYellow,
        playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
    )
}
