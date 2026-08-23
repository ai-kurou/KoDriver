package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.Simulator
import org.junit.Test

class SimulatorSwitcherScreenshotTest {
    @Test
    fun `未選択状態`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                            SimulatorSwitcher(
                                selectedSimulator = null,
                                simulators = Simulator.entries,
                                onSimulatorSelected = {},
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `選択済み状態`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                            SimulatorSwitcher(
                                selectedSimulator = Simulator.LmuWindows,
                                simulators = Simulator.entries,
                                onSimulatorSelected = {},
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
