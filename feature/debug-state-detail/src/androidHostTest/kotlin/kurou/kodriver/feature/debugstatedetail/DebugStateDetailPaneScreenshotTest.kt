@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.github.takahirom.roborazzi.captureRoboImage
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

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

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w480dp-h640dp")
class DebugStateDetailPaneScreenshotTest {

    @Test
    fun `デフォルト`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        DebugStateDetailPaneContent(
                            uiState = DebugStateDetailUiState(),
                            canNavigateBack = true,
                            onBack = {},
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `フラグ情報取得済み`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        DebugStateDetailPaneContent(
                            uiState = DebugStateDetailUiState(raceFlags = sampleRaceFlags),
                            canNavigateBack = true,
                            onBack = {},
                        )
                    }
                }
            }
        }
    }
}
