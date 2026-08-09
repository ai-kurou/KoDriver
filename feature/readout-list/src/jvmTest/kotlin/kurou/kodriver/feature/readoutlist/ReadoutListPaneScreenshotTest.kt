package kurou.kodriver.feature.readoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import org.junit.Test
import kotlin.test.BeforeTest

class ReadoutListPaneScreenshotTest {
    @MockK
    private lateinit var repository: ReadoutPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            ReadoutListPane(
                                uiState =
                                    ReadoutListUiState(
                                        simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                                    ),
                                onSimulatorSelected = {},
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onItemClick = { _ -> },
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `lmu_windows選択`() =
        composeScreenshotTest {
            every { repository.observeReadoutEnabledStates("lmu_windows") } returns MutableStateFlow(emptyMap())
            val enabledStates = observeReadoutEnabledStates("lmu_windows")

            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            ReadoutListPane(
                                uiState =
                                    ReadoutListUiState(
                                        simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                                        selectedSimulator = Simulator.LmuWindows,
                                        items = ReadoutListItemType.defaultOrder(Simulator.LmuWindows),
                                        readoutEnabledStates = enabledStates,
                                    ),
                                onSimulatorSelected = {},
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onItemClick = { _ -> },
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
            verify(exactly = 1) { repository.observeReadoutEnabledStates("lmu_windows") }
            confirmVerified(repository)
        }

    @Test
    fun `gt7_ps5選択`() =
        composeScreenshotTest {
            every { repository.observeReadoutEnabledStates("gt7_ps5") } returns MutableStateFlow(emptyMap())
            val enabledStates = observeReadoutEnabledStates("gt7_ps5")

            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            ReadoutListPane(
                                uiState =
                                    ReadoutListUiState(
                                        simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                                        selectedSimulator = Simulator.Gt7Ps5,
                                        items = ReadoutListItemType.defaultOrder(Simulator.Gt7Ps5),
                                        readoutEnabledStates = enabledStates,
                                    ),
                                onSimulatorSelected = {},
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onItemClick = { _ -> },
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
            verify(exactly = 1) { repository.observeReadoutEnabledStates("gt7_ps5") }
            confirmVerified(repository)
        }

    @Test
    fun `ace_windows選択`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            ReadoutListPane(
                                uiState =
                                    ReadoutListUiState(
                                        simulators =
                                            listOf(
                                                Simulator.LmuWindows,
                                                Simulator.Gt7Ps5,
                                                Simulator.AceWindows,
                                            ),
                                        selectedSimulator = Simulator.AceWindows,
                                        items = ReadoutListItemType.defaultOrder(Simulator.AceWindows),
                                    ),
                                onSimulatorSelected = {},
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onItemClick = { _ -> },
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    private fun observeReadoutEnabledStates(simulator: String): Map<ReadoutItemKey, Boolean> =
        runBlocking { ObserveReadoutEnabledStatesUseCase(repository)(simulator).first() }
}
