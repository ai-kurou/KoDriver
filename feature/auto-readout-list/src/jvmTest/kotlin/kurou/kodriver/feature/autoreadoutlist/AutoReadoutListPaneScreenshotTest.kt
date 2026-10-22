package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import org.junit.Test

class AutoReadoutListPaneScreenshotTest {
    init {
        registerAllReadoutItemKeys()
    }

    private val repository: AutoReadoutPreferencesRepository = mockk()

    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            AutoReadoutListPane(
                                uiState = AutoReadoutListUiState(),
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onStartSoundEnabledChanged = { _, _ -> },
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
            every { repository.observeAutoReadoutEnabledStates("lmu_windows") } returns MutableStateFlow(emptyMap())
            val enabledStates = observeAutoReadoutEnabledStates("lmu_windows")

            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            AutoReadoutListPane(
                                uiState =
                                    AutoReadoutListUiState(
                                        selectedSimulator = Simulator.LmuWindows,
                                        items = AutoReadoutListItemType.defaultOrder(Simulator.LmuWindows),
                                        autoReadoutEnabledStates = enabledStates,
                                    ),
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onStartSoundEnabledChanged = { _, _ -> },
                                onItemClick = { _ -> },
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
            verify(exactly = 1) { repository.observeAutoReadoutEnabledStates("lmu_windows") }
            confirmVerified(repository)
        }

    @Test
    fun `gt7_ps5選択`() =
        composeScreenshotTest {
            every { repository.observeAutoReadoutEnabledStates("gt7_ps5") } returns MutableStateFlow(emptyMap())
            val enabledStates = observeAutoReadoutEnabledStates("gt7_ps5")

            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            AutoReadoutListPane(
                                uiState =
                                    AutoReadoutListUiState(
                                        selectedSimulator = Simulator.Gt7Ps5,
                                        items = AutoReadoutListItemType.defaultOrder(Simulator.Gt7Ps5),
                                        autoReadoutEnabledStates = enabledStates,
                                    ),
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onStartSoundEnabledChanged = { _, _ -> },
                                onItemClick = { _ -> },
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
            verify(exactly = 1) { repository.observeAutoReadoutEnabledStates("gt7_ps5") }
            confirmVerified(repository)
        }

    @Test
    fun `ace_windows選択`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            AutoReadoutListPane(
                                uiState =
                                    AutoReadoutListUiState(
                                        selectedSimulator = Simulator.AceWindows,
                                        items = AutoReadoutListItemType.defaultOrder(Simulator.AceWindows),
                                    ),
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onStartSoundEnabledChanged = { _, _ -> },
                                onItemClick = { _ -> },
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `選択状態の切り替え中に枠線と影を表示する`() =
        composeScreenshotTest {
            val selectedItem = mutableStateOf<AutoReadoutListItemType?>(null)
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            AutoReadoutListPane(
                                uiState =
                                    AutoReadoutListUiState(
                                        selectedSimulator = Simulator.LmuWindows,
                                        items = listOf(LmuWindowsReadoutItemKey.Flag.Root),
                                        selectedItem = selectedItem.value,
                                    ),
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onStartSoundEnabledChanged = { _, _ -> },
                                onItemClick = { _ -> },
                            )
                        }
                    }
                }
            }

            mainClock.autoAdvance = false
            selectedItem.value = LmuWindowsAutoReadoutListItemType.Flag
            mainClock.advanceTimeBy(250)

            onRoot().captureRoboImage()
        }

    @Test
    fun `リストを下にスクロールすると先頭へ戻るボタンを表示する`() =
        composeScreenshotTest {
            every { repository.observeAutoReadoutEnabledStates("lmu_windows") } returns MutableStateFlow(emptyMap())
            val enabledStates = observeAutoReadoutEnabledStates("lmu_windows")
            val items = AutoReadoutListItemType.defaultOrder(Simulator.LmuWindows)
            var lastItemText = ""

            setContent {
                lastItemText = autoReadoutItemDisplayName(items.last())
                KoDriverTheme {
                    Surface {
                        // 先頭へ戻るボタンの表示にはスクロールが必要なため、listPane標準サイズ(360x1080)より低くしている
                        Box(modifier = Modifier.requiredSize(360.dp, 400.dp)) {
                            AutoReadoutListPane(
                                uiState =
                                    AutoReadoutListUiState(
                                        selectedSimulator = Simulator.LmuWindows,
                                        items = items,
                                        autoReadoutEnabledStates = enabledStates,
                                    ),
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onStartSoundEnabledChanged = { _, _ -> },
                                onItemClick = { _ -> },
                            )
                        }
                    }
                }
            }
            onNode(hasScrollAction()).performScrollToNode(hasText(lastItemText))
            waitForIdle()

            onRoot().captureRoboImage()
            verify(exactly = 1) { repository.observeAutoReadoutEnabledStates("lmu_windows") }
            confirmVerified(repository)
        }

    @Test
    fun `ヘルプボトムシート`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Column(modifier = Modifier.requiredSize(480.dp, 400.dp)) {
                            PriorityHintSheetContent()
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `ライトテーマでONとOFFと選択枠を表示する`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme(darkTheme = false) {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            AutoReadoutListPane(
                                uiState =
                                    AutoReadoutListUiState(
                                        selectedSimulator = Simulator.LmuWindows,
                                        items = AutoReadoutListItemType.defaultOrder(Simulator.LmuWindows),
                                        selectedItem = LmuWindowsAutoReadoutListItemType.Flag,
                                        autoReadoutEnabledStates =
                                            mapOf(
                                                LmuWindowsReadoutItemKey.VehicleApproach.Root to true,
                                                LmuWindowsReadoutItemKey.Flag.Root to true,
                                            ),
                                        startSoundEnabledStates =
                                            mapOf(LmuWindowsReadoutItemKey.Flag.Root to false),
                                        queueEnabledStates =
                                            mapOf(LmuWindowsReadoutItemKey.VehicleApproach.Root to true),
                                    ),
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onStartSoundEnabledChanged = { _, _ -> },
                                onItemClick = { _ -> },
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `ダークテーマでONとOFFと選択枠を表示する`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme(darkTheme = true) {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            AutoReadoutListPane(
                                uiState =
                                    AutoReadoutListUiState(
                                        selectedSimulator = Simulator.LmuWindows,
                                        items = AutoReadoutListItemType.defaultOrder(Simulator.LmuWindows),
                                        selectedItem = LmuWindowsAutoReadoutListItemType.Flag,
                                        autoReadoutEnabledStates =
                                            mapOf(
                                                LmuWindowsReadoutItemKey.VehicleApproach.Root to true,
                                                LmuWindowsReadoutItemKey.Flag.Root to true,
                                            ),
                                        startSoundEnabledStates =
                                            mapOf(LmuWindowsReadoutItemKey.Flag.Root to false),
                                        queueEnabledStates =
                                            mapOf(LmuWindowsReadoutItemKey.VehicleApproach.Root to true),
                                    ),
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onStartSoundEnabledChanged = { _, _ -> },
                                onItemClick = { _ -> },
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    private fun observeAutoReadoutEnabledStates(simulator: String): Map<ReadoutItemKey, Boolean> =
        runBlocking { ObserveAutoReadoutEnabledStatesUseCase(repository)(simulator).first() }
}
