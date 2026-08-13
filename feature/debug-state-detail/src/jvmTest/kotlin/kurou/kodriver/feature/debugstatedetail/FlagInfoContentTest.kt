package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test

class FlagInfoContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `selectedSimulatorが未選択の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = null,
                            cardOrder = listOf(DebugStateCardKey.FLAG_INFO),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("フラグ情報").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがGT7の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.Gt7Ps5,
                            cardOrder = listOf(DebugStateCardKey.FLAG_INFO),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUでraceFlagsがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            raceFlags = null,
                            cardOrder = listOf(DebugStateCardKey.FLAG_INFO),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUでアクティブなフラグがない場合はなしの文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            raceFlags = sampleRaceFlags(),
                            cardOrder = listOf(DebugStateCardKey.FLAG_INFO),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("フラッグなし").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUでアクティブなフラグがある場合はその名称を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            raceFlags = sampleRaceFlags(playerFlag = PrimaryFlag.BLUE),
                            cardOrder = listOf(DebugStateCardKey.FLAG_INFO),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ブルーフラッグ").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがACEでaceWindowsFlagがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.AceWindows,
                            aceWindowsFlag = null,
                            cardOrder = listOf(DebugStateCardKey.FLAG_INFO),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがACEの場合はフラグの表示名を表示する`() {
        val expected =
            mapOf(
                AceWindowsFlagType.NO_FLAG to "フラッグなし",
                AceWindowsFlagType.WHITE_FLAG to "ホワイトフラッグ",
                AceWindowsFlagType.GREEN_FLAG to "グリーンフラッグ",
                AceWindowsFlagType.RED_FLAG to "レッドフラッグ",
                AceWindowsFlagType.BLUE_FLAG to "ブルーフラッグ",
                AceWindowsFlagType.YELLOW_FLAG to "イエローフラッグ",
                AceWindowsFlagType.BLACK_FLAG to "ブラックフラッグ",
                AceWindowsFlagType.BLACK_WHITE_FLAG to "ブラック・ホワイトフラッグ",
                AceWindowsFlagType.CHECKERED_FLAG to "チェッカーフラッグ",
                AceWindowsFlagType.ORANGE_CIRCLE_FLAG to "オレンジボールフラッグ",
                AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG to "レッド・イエローストライプフラッグ",
                AceWindowsFlagType.UNKNOWN to "未取得",
            )

        expected.forEach { (flag, displayName) ->
            rule.setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = Simulator.AceWindows,
                                aceWindowsFlag = AceWindowsFlagData(flag = flag),
                                cardOrder = listOf(DebugStateCardKey.FLAG_INFO),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            rule.onNodeWithText(displayName).assertIsDisplayed()
        }
    }

    private fun sampleRaceFlags(playerFlag: PrimaryFlag = PrimaryFlag.GREEN) =
        LmuWindowsRaceFlagsData(
            gamePhase = SessionPhase.GREEN_FLAG,
            yellowFlagState = SessionYellowFlagState.NONE,
            sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
            startLight = 0,
            numRedLights = 0,
            playerFlag = playerFlag,
            playerUnderYellow = false,
            playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
        )
}
