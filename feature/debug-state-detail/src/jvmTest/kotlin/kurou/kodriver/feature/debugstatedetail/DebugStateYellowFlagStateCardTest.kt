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

class DebugStateYellowFlagStateCardTest {
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
                            cardOrder = listOf(DebugStateCardKey.YELLOW_FLAG_STATE),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("イエローフラッグ状態 (LMUのみ)").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `yellowFlagStateの各値に対応する表示名を表示する`() {
        val expected =
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

        expected.forEach { (yellowFlagState, displayName) ->
            rule.setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                raceFlags = sampleRaceFlags(yellowFlagState = yellowFlagState),
                                cardOrder = listOf(DebugStateCardKey.YELLOW_FLAG_STATE),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            rule.onNodeWithText(displayName).assertIsDisplayed()
        }
    }

    private fun sampleRaceFlags(yellowFlagState: SessionYellowFlagState) =
        LmuWindowsRaceFlagsData(
            gamePhase = SessionPhase.GREEN_FLAG,
            yellowFlagState = yellowFlagState,
            sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
            startLight = 0,
            numRedLights = 0,
            playerFlag = PrimaryFlag.GREEN,
            playerUnderYellow = false,
            playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
        )
}
