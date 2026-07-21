package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import org.junit.Rule
import org.junit.Test

private val sampleRaceFlags = LmuWindowsRaceFlagsData(
    gamePhase = SessionPhase.GREEN_FLAG,
    yellowFlagState = SessionYellowFlagState.NONE,
    sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.YELLOW, SectorFlagState.CLEAR),
    startLight = 0,
    numRedLights = 0,
    playerFlag = PrimaryFlag.GREEN,
    playerUnderYellow = false,
    playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
)

class DebugStateDetailPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        DebugStateDetailPaneContent(
                            uiState = DebugStateDetailUiState(),
                            canNavigateBack = true,
                            onBack = {},
                        )
                    }
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `フラグ情報取得済み`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        DebugStateDetailPaneContent(
                            uiState = DebugStateDetailUiState(raceFlags = sampleRaceFlags),
                            canNavigateBack = true,
                            onBack = {},
                        )
                    }
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }
}
