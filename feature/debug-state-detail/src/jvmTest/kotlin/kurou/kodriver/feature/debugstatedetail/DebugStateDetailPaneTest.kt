package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.Simulator
import org.junit.Test
import kotlin.test.assertEquals

class DebugStateDetailPaneTest {
    @Test
    fun `タイトルを表示して戻る操作を通知する`() =
        composeScreenshotTest {
            var backCount = 0
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState = DebugStateDetailUiState(),
                        canNavigateBack = true,
                        onBack = { backCount++ },
                    )
                }
            }

            onNodeWithText("デバッグ状態").assertIsDisplayed()
            onNode(hasContentDescription("戻る")).performClick()

            assertEquals(1, backCount)
        }

    @Test
    fun `フラグ情報・ゲームフェーズ・セッションが未取得の場合は未取得の文言を表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState = DebugStateDetailUiState(),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onAllNodesWithText("未取得").assertCountEquals(14)
        }

    @Test
    fun `イエローフラッグ状態カードのタイトルを表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState = DebugStateDetailUiState(),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("イエローフラッグ状態 (LMUのみ)").assertIsDisplayed()
        }

    @Test
    fun `yellowFlagStateの各値に対応する表示文言を表示する`() =
        composeScreenshotTest {
            val expectedByYellowFlagState =
                mapOf(
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
                setContent {
                    MaterialTheme {
                        DebugStateDetailPaneContent(
                            uiState =
                                DebugStateDetailUiState(
                                    raceFlags = sampleRaceFlags(yellowFlagState = yellowFlagState),
                                ),
                            canNavigateBack = true,
                            onBack = {},
                        )
                    }
                }

                onNodeWithText(expectedText).assertIsDisplayed()
            }
        }

    @Test
    fun `セッションカードのタイトルを表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState = DebugStateDetailUiState(),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("セッション (LMUのみ)").assertIsDisplayed()
        }

    @Test
    fun `sessionの各値に対応する表示文言を表示する`() =
        composeScreenshotTest {
            val expectedBySession =
                mapOf(
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
                setContent {
                    MaterialTheme {
                        DebugStateDetailPaneContent(
                            uiState =
                                DebugStateDetailUiState(
                                    virtualEnergy =
                                        LmuWindowsVirtualEnergyData(
                                            remainingRatio = 0.5,
                                            session = session,
                                        ),
                                ),
                            canNavigateBack = true,
                            onBack = {},
                        )
                    }
                }

                onNodeWithText(expectedText).assertIsDisplayed()
            }
        }

    @Test
    fun `ゲームフェーズカードのタイトルを表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState = DebugStateDetailUiState(),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("ゲームフェーズ (LMUのみ)").assertIsDisplayed()
        }

    @Test
    fun `gamePhaseの各値に対応する表示文言を表示する`() =
        composeScreenshotTest {
            val expectedByGamePhase =
                mapOf(
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
                setContent {
                    MaterialTheme {
                        DebugStateDetailPaneContent(
                            uiState = DebugStateDetailUiState(raceFlags = sampleRaceFlags(gamePhase = gamePhase)),
                            canNavigateBack = true,
                            onBack = {},
                        )
                    }
                }

                onNodeWithText(expectedText).assertIsDisplayed()
            }
        }

    @Test
    fun `選択中のシミュレータが未選択の場合は未選択の文言を表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState = DebugStateDetailUiState(),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("未選択").assertIsDisplayed()
        }

    @Test
    fun `選択中のシミュレータがLMUの場合は表示名を表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState = DebugStateDetailUiState(selectedSimulator = Simulator.LmuWindows),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("Le Mans Ultimate（Windows版）").assertIsDisplayed()
        }

    @Test
    fun `フラグ情報が取得済みでアクティブなフラグがない場合はフラッグなしを表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = Simulator.LmuWindows,
                                raceFlags =
                                    LmuWindowsRaceFlagsData(
                                        gamePhase = SessionPhase.GREEN_FLAG,
                                        yellowFlagState = SessionYellowFlagState.NONE,
                                        sectorFlags =
                                            listOf(
                                                SectorFlagState.CLEAR,
                                                SectorFlagState.CLEAR,
                                                SectorFlagState.CLEAR,
                                            ),
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

            onNodeWithText("フラッグなし").assertIsDisplayed()
        }

    @Test
    fun `ブルーフラッグが出ている場合はブルーフラッグを表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = Simulator.LmuWindows,
                                raceFlags =
                                    LmuWindowsRaceFlagsData(
                                        gamePhase = SessionPhase.GREEN_FLAG,
                                        yellowFlagState = SessionYellowFlagState.NONE,
                                        sectorFlags =
                                            listOf(
                                                SectorFlagState.CLEAR,
                                                SectorFlagState.CLEAR,
                                                SectorFlagState.CLEAR,
                                            ),
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

            onNodeWithText("ブルーフラッグ").assertIsDisplayed()
        }

    @Test
    fun `選択中のシミュレータがACEの場合はACEフラッグ種別ごとの表示文言を表示する`() =
        composeScreenshotTest {
            val expectedByAceFlag =
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
                )

            expectedByAceFlag.forEach { (flag, expectedText) ->
                setContent {
                    MaterialTheme {
                        DebugStateDetailPaneContent(
                            uiState =
                                DebugStateDetailUiState(
                                    selectedSimulator = Simulator.AceWindows,
                                    aceWindowsFlag = AceWindowsFlagData(flag = flag),
                                ),
                            canNavigateBack = true,
                            onBack = {},
                        )
                    }
                }

                onNodeWithText(expectedText).assertIsDisplayed()
            }
        }

    @Test
    fun `選択中のシミュレータがACEでフラッグ情報が未取得またはUNKNOWNの場合は未取得の文言を表示する`() =
        composeScreenshotTest {
            listOf(null, AceWindowsFlagData(flag = AceWindowsFlagType.UNKNOWN)).forEach { aceWindowsFlag ->
                setContent {
                    MaterialTheme {
                        DebugStateDetailPaneContent(
                            uiState =
                                DebugStateDetailUiState(
                                    selectedSimulator = Simulator.AceWindows,
                                    aceWindowsFlag = aceWindowsFlag,
                                ),
                            canNavigateBack = true,
                            onBack = {},
                        )
                    }
                }

                onAllNodesWithText("未取得").assertCountEquals(14)
            }
        }

    @Test
    fun `cardOrderを入れ替えても両方のカードが表示される`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                cardOrder = listOf(DebugStateCardKey.FLAG_INFO, DebugStateCardKey.SIMULATOR),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("フラグ情報").assertIsDisplayed()
            onNodeWithText("選択中のシミュレータ").assertIsDisplayed()
        }

    @Test
    fun `幅400dp未満では列数は1`() =
        composeScreenshotTest {
            assertEquals(1, calculateDebugStateColumns(399.dp))
        }

    @Test
    fun `幅400dp以上700dp未満では列数は2`() =
        composeScreenshotTest {
            assertEquals(2, calculateDebugStateColumns(400.dp))
            assertEquals(2, calculateDebugStateColumns(699.dp))
        }

    @Test
    fun `幅700dp以上では列数は3`() =
        composeScreenshotTest {
            assertEquals(3, calculateDebugStateColumns(700.dp))
        }

    private fun sampleRaceFlags(
        gamePhase: SessionPhase = SessionPhase.GREEN_FLAG,
        yellowFlagState: SessionYellowFlagState = SessionYellowFlagState.NONE,
        sectorFlags: List<SectorFlagState> =
            listOf(
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
