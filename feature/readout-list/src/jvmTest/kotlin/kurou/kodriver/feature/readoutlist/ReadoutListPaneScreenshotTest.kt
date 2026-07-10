package kurou.kodriver.feature.readoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
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
        val observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(FakeReadoutPreferencesRepository())
        val enabledStates = runBlocking { observeReadoutEnabledStates("lmu_windows").first() }

        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                        ReadoutListPane(
                            uiState = ReadoutListUiState(
                                simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                                selectedSimulator = Simulator.LmuWindows,
                                items = ReadoutListItemType.defaultOrder(Simulator.LmuWindows),
                                readoutEnabledStates = enabledStates,
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
        val observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(FakeReadoutPreferencesRepository())
        val enabledStates = runBlocking { observeReadoutEnabledStates("gt7_ps5").first() }

        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                        ReadoutListPane(
                            uiState = ReadoutListUiState(
                                simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                                selectedSimulator = Simulator.Gt7Ps5,
                                items = ReadoutListItemType.defaultOrder(Simulator.Gt7Ps5),
                                readoutEnabledStates = enabledStates,
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
