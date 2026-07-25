package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
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
    fun `フラグ情報・ゲームフェーズ・セッションが未取得の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onAllNodesWithText("未取得").assertCountEquals(11)
    }

    @Test
    fun `mYellowFlagStateカードのタイトルを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("mYellowFlagState").assertIsDisplayed()
    }

    @Test
    fun `yellowFlagStateの各値に対応する表示文言を表示する`() {
        val expectedByYellowFlagState = mapOf(
            SessionYellowFlagState.INVALID to "Invalid",
            SessionYellowFlagState.NONE to "None（グリーン）",
            SessionYellowFlagState.PENDING to "Pending（FCY 発動保留）",
            SessionYellowFlagState.PIT_CLOSED to "PitClosed（ピットクローズ）",
            SessionYellowFlagState.PIT_LEAD_LAP to "PitLeadLap（先頭周回のみピット可）",
            SessionYellowFlagState.PIT_OPEN to "PitOpen（ピットオープン）",
            SessionYellowFlagState.LAST_LAP to "LastLap（最終周）",
            SessionYellowFlagState.RESUME to "Resume（リスタート）",
            SessionYellowFlagState.RACE_HALT to "RaceHalt（レース中断、現在未使用）",
            SessionYellowFlagState.UNKNOWN to "不明",
        )

        expectedByYellowFlagState.forEach { (yellowFlagState, expectedText) ->
            rule.setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState = DebugStateDetailUiState(
                            raceFlags = sampleRaceFlags(yellowFlagState = yellowFlagState),
                        ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            rule.onNodeWithText(expectedText).assertIsDisplayed()
        }
    }

    @Test
    fun `mSessionカードのタイトルを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("mSession").assertIsDisplayed()
    }

    @Test
    fun `sessionの各値に対応する表示文言を表示する`() {
        val expectedBySession = mapOf(
            0 to "Test Day（テスト走行）",
            1 to "Practice（練習走行）",
            4 to "Practice（練習走行）",
            5 to "Qualifying（予選）",
            8 to "Qualifying（予選）",
            9 to "Warmup（ウォームアップ）",
            10 to "Race（決勝）",
            13 to "Race（決勝）",
            -1 to "不明",
        )

        expectedBySession.forEach { (session, expectedText) ->
            rule.setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState = DebugStateDetailUiState(
                            virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = 0.5, session = session),
                        ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            rule.onNodeWithText(expectedText).assertIsDisplayed()
        }
    }

    @Test
    fun `mGamePhaseカードのタイトルを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("mGamePhase").assertIsDisplayed()
    }

    @Test
    fun `gamePhaseの各値に対応する表示文言を表示する`() {
        val expectedByGamePhase = mapOf(
            SessionPhase.GARAGE to "セッション開始前",
            SessionPhase.WARM_UP to "Reconnaissance laps（レースのみ）",
            SessionPhase.GRID_WALK to "GridWalk（グリッドウォーク、レースのみ）",
            SessionPhase.FORMATION to "Formation（フォーメーションラップ、レースのみ）",
            SessionPhase.COUNTDOWN to "Countdown（スタートライト点灯開始、レースのみ）",
            SessionPhase.GREEN_FLAG to "GreenFlag（グリーンフラッグ）",
            SessionPhase.FULL_COURSE_YELLOW to "FullCourseYellow（FCY / セーフティカー）",
            SessionPhase.RED_FLAG to "SessionStopped（セッション停止）",
            SessionPhase.SESSION_OVER to "SessionOver（セッション終了）",
            SessionPhase.PAUSED_OR_HEARTBEAT to "Paused（ポーズ中。プラグインへのハートビート呼び出し）",
            SessionPhase.UNKNOWN to "不明",
        )

        expectedByGamePhase.forEach { (gamePhase, expectedText) ->
            rule.setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState = DebugStateDetailUiState(raceFlags = sampleRaceFlags(gamePhase = gamePhase)),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            rule.onNodeWithText(expectedText).assertIsDisplayed()
        }
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
    fun `cardOrderを入れ替えても両方のカードが表示される`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        cardOrder = listOf(DebugStateCardKey.FLAG_INFO, DebugStateCardKey.SIMULATOR),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("フラグ情報").assertIsDisplayed()
        rule.onNodeWithText("選択中のシミュレータ").assertIsDisplayed()
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

    private fun sampleRaceFlags(
        gamePhase: SessionPhase = SessionPhase.GREEN_FLAG,
        yellowFlagState: SessionYellowFlagState = SessionYellowFlagState.NONE,
        sectorFlags: List<SectorFlagState> = listOf(
            SectorFlagState.CLEAR,
            SectorFlagState.CLEAR,
            SectorFlagState.CLEAR,
        ),
        playerFlag: PrimaryFlag = PrimaryFlag.GREEN,
        playerUnderYellow: Boolean = false,
    ) = LmuWindowsRaceFlagsData(
        gamePhase = gamePhase,
        yellowFlagState = yellowFlagState,
        sectorFlags = sectorFlags,
        startLight = 0,
        numRedLights = 0,
        playerFlag = playerFlag,
        playerUnderYellow = playerUnderYellow,
        playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
    )
}
