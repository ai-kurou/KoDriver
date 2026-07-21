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
    fun `フラグ情報が取得済みの場合は各フィールドを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        raceFlags = LmuWindowsRaceFlagsData(
                            gamePhase = SessionPhase.GREEN_FLAG,
                            yellowFlagState = SessionYellowFlagState.NONE,
                            sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.YELLOW, SectorFlagState.CLEAR),
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

        rule.onNodeWithText("gamePhase: GREEN_FLAG").assertIsDisplayed()
        rule.onNodeWithText("playerUnderYellow: false").assertIsDisplayed()
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
}
