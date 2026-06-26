package kurou.kodriver.feature.readoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.domain.model.ReadoutItemKey
import org.junit.Rule
import org.junit.Test

class ReadoutListPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                        ReadoutListPane(
                            uiState = ReadoutListUiState(
                                simulators = listOf("lmu_windows", "gt7_ps5"),
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
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                        ReadoutListPane(
                            uiState = ReadoutListUiState(
                                simulators = listOf("lmu_windows", "gt7_ps5"),
                                selectedSimulator = "lmu_windows",
                                items = listOf(
                                    ReadoutItemKey.VEHICLE_APPROACH,
                                    ReadoutItemKey.FLAG,
                                    ReadoutItemKey.VEHICLE_DAMAGE,
                                ),
                                readoutEnabledStates = mapOf(
                                    ReadoutItemKey.VEHICLE_APPROACH to true,
                                    ReadoutItemKey.FLAG to true,
                                    ReadoutItemKey.VEHICLE_DAMAGE to true,
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
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                        ReadoutListPane(
                            uiState = ReadoutListUiState(
                                simulators = listOf("lmu_windows", "gt7_ps5"),
                                selectedSimulator = "gt7_ps5",
                                items = listOf(
                                    ReadoutItemKey.MY_BEST_LAP,
                                    ReadoutItemKey.REMAINING_FUEL_LAPS,
                                ),
                                readoutEnabledStates = mapOf(
                                    ReadoutItemKey.MY_BEST_LAP to true,
                                    ReadoutItemKey.REMAINING_FUEL_LAPS to true,
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
