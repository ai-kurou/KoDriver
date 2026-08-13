package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import org.junit.Rule
import org.junit.Test

class DebugStateGamePhaseCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `raceFlagsがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            raceFlags = null,
                            cardOrder = listOf(DebugStateCardKey.GAME_PHASE),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ゲームフェーズ (LMUのみ)").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `gamePhaseの各値に対応する表示名を表示する`() {
        val expected =
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

        expected.forEach { (gamePhase, displayName) ->
            rule.setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                raceFlags = sampleRaceFlags(gamePhase = gamePhase),
                                cardOrder = listOf(DebugStateCardKey.GAME_PHASE),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            rule.onNodeWithText(displayName).assertIsDisplayed()
        }
    }

    private fun sampleRaceFlags(gamePhase: SessionPhase) =
        LmuWindowsRaceFlagsData(
            gamePhase = gamePhase,
            yellowFlagState = SessionYellowFlagState.NONE,
            sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
            startLight = 0,
            numRedLights = 0,
            playerFlag = PrimaryFlag.GREEN,
            playerUnderYellow = false,
            playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
        )
}
