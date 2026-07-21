package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DebugStateDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルを表示して戻る操作を通知する`() {
        var backCount = 0
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = { backCount++ },
                )
            }
        }

        rule.onNodeWithText("デバッグ状態").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }

    @Test
    fun `フラグ情報が未取得の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `選択中のシミュレータが未選択の場合は未選択の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("未選択").assertIsDisplayed()
    }

    @Test
    fun `選択中のシミュレータがLMUの場合は表示名を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(selectedSimulator = Simulator.LmuWindows),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("Le Mans Ultimate（Windows版）").assertIsDisplayed()
    }

    @Test
    fun `フラグ情報が取得済みでアクティブなフラグがない場合はフラッグなしを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        raceFlags = LmuWindowsRaceFlagsData(
                            gamePhase = SessionPhase.GREEN_FLAG,
                            yellowFlagState = SessionYellowFlagState.NONE,
                            sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
                            startLight = 0,
                            numRedLights = 0,
                            playerFlag = PrimaryFlag.GREEN,
                            playerUnderYellow = false,
                            playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
                        ),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("フラッグなし").assertIsDisplayed()
    }

    @Test
    fun `ブルーフラッグが出ている場合はブルーフラッグを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        raceFlags = LmuWindowsRaceFlagsData(
                            gamePhase = SessionPhase.GREEN_FLAG,
                            yellowFlagState = SessionYellowFlagState.NONE,
                            sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
                            startLight = 0,
                            numRedLights = 0,
                            playerFlag = PrimaryFlag.BLUE,
                            playerUnderYellow = false,
                            playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
                        ),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ブルーフラッグ").assertIsDisplayed()
    }

    @Test
    fun `幅400dp未満では列数は1`() {
        assertEquals(1, calculateDebugStateColumns(399.dp))
    }

    @Test
    fun `幅400dp以上700dp未満では列数は2`() {
        assertEquals(2, calculateDebugStateColumns(400.dp))
        assertEquals(2, calculateDebugStateColumns(699.dp))
    }

    @Test
    fun `幅700dp以上では列数は3`() {
        assertEquals(3, calculateDebugStateColumns(700.dp))
    }

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
