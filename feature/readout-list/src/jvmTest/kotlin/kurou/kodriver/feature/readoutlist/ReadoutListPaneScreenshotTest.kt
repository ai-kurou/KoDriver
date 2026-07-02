package kurou.kodriver.feature.readoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test

class ReadoutListPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                        ReadoutListPane(
                            uiState = ReadoutListUiState(
                                simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                            ),
                            onSimulatorSelected = {},
                            onMove = { _, _ -> },
                            onReadoutEnabledChanged = { _, _ -> },
                            onItemClick = { _ -> },
                        )
                    }
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `lmu_windows選択`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                        ReadoutListPane(
                            uiState = ReadoutListUiState(
                                simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                                selectedSimulator = Simulator.LmuWindows,
                                items = listOf(
                                    ReadoutItemKey.VehicleApproach,
                                    ReadoutItemKey.Flag,
                                    ReadoutItemKey.VehicleDamage,
                                ),
                                readoutEnabledStates = mapOf(
                                    ReadoutItemKey.VehicleApproach to true,
                                    ReadoutItemKey.Flag to true,
                                    ReadoutItemKey.VehicleDamage to true,
                                ),
                            ),
                            onSimulatorSelected = {},
                            onMove = { _, _ -> },
                            onReadoutEnabledChanged = { _, _ -> },
                            onItemClick = { _ -> },
                        )
                    }
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `gt7_ps5選択`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                        ReadoutListPane(
                            uiState = ReadoutListUiState(
                                simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                                selectedSimulator = Simulator.Gt7Ps5,
                                items = listOf(
                                    ReadoutItemKey.RemainingFuelLaps,
                                    ReadoutItemKey.MyBestLap,
                                ),
                                readoutEnabledStates = mapOf(
                                    ReadoutItemKey.MyBestLap to true,
                                    ReadoutItemKey.RemainingFuelLaps to true,
                                ),
                            ),
                            onSimulatorSelected = {},
                            onMove = { _, _ -> },
                            onReadoutEnabledChanged = { _, _ -> },
                            onItemClick = { _ -> },
                        )
                    }
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }
}
