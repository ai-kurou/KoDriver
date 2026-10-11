package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FlagInfoContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `raceFlagsがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.FLAG_INFO),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    raceFlags = null,
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `アクティブなフラグがない場合はなしの文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.FLAG_INFO),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    raceFlags = sampleRaceFlags(),
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
    fun `アクティブなフラグがある場合はその名称を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.FLAG_INFO),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    raceFlags = sampleRaceFlags(playerFlag = PrimaryFlag.BLUE),
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
    fun `イエローとフルコースイエローとレッドの名称を表示する`() {
        val flagsByLabel =
            mapOf(
                "イエローフラッグ" to sampleRaceFlags().copy(playerUnderYellow = true),
                "フルコースイエロー" to sampleRaceFlags().copy(gamePhase = SessionPhase.FULL_COURSE_YELLOW),
                "レッドフラッグ" to sampleRaceFlags().copy(gamePhase = SessionPhase.RED_FLAG),
            )
        flagsByLabel.forEach { (label, flags) ->
            rule.setContent {
                MaterialTheme {
                    FlagInfoContent(flags)
                }
            }

            rule.onNodeWithText(label).assertIsDisplayed()
        }
    }

    private fun sampleRaceFlags(playerFlag: PrimaryFlag = PrimaryFlag.GREEN) =
        LmuWindowsRaceFlagsData(
            gamePhase = SessionPhase.GREEN_FLAG,
            yellowFlagState = SessionYellowFlagState.NONE,
            sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
            playerFlag = playerFlag,
            playerUnderYellow = false,
        )

    @Test
    fun `表示するフラグが同じならyellowFlagStateが変わってもコンテンツキーは変わらない`() {
        val before = sampleRaceFlags(PrimaryFlag.GREEN)
        val after = before.copy(yellowFlagState = SessionYellowFlagState.PENDING)

        assertEquals(
            flagInfoContentKey(before),
            flagInfoContentKey(after),
        )
    }

    @Test
    fun `表示するフラグが変わるとコンテンツキーも変わる`() {
        assertNotEquals(
            flagInfoContentKey(sampleRaceFlags(PrimaryFlag.GREEN)),
            flagInfoContentKey(sampleRaceFlags(PrimaryFlag.BLUE)),
        )
        assertNotEquals(
            flagInfoContentKey(null),
            flagInfoContentKey(sampleRaceFlags(PrimaryFlag.GREEN)),
        )
    }
}
