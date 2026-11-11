package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Test

/**
 * 手動読み上げ一覧を、シミュレーター3種 × セットなし/あり × 標準表示/再生ボタン表示の12通りで撮影する。
 * 再生ボタン表示は、見出し右上の切替ボタンを押した後の状態を撮る。
 */
class ManualReadoutListPaneLayoutScreenshotTest {
    init {
        registerAllManualReadoutSimulators()
    }

    @Test
    fun `LMUのセットなし標準表示`() = captureListPane(Simulator.LMU, emptyList(), switchToPlayButtons = false)

    @Test
    fun `LMUのセットあり標準表示`() = captureListPane(Simulator.LMU, lmuSets(), switchToPlayButtons = false)

    @Test
    fun `LMUのセットなし再生ボタン表示`() = captureListPane(Simulator.LMU, emptyList(), switchToPlayButtons = true)

    @Test
    fun `LMUのセットあり再生ボタン表示`() = captureListPane(Simulator.LMU, lmuSets(), switchToPlayButtons = true)

    @Test
    fun `GT7のセットなし標準表示`() = captureListPane(Simulator.GT7, emptyList(), switchToPlayButtons = false)

    @Test
    fun `GT7のセットあり標準表示`() = captureListPane(Simulator.GT7, gt7Sets(), switchToPlayButtons = false)

    @Test
    fun `GT7のセットなし再生ボタン表示`() = captureListPane(Simulator.GT7, emptyList(), switchToPlayButtons = true)

    @Test
    fun `GT7のセットあり再生ボタン表示`() = captureListPane(Simulator.GT7, gt7Sets(), switchToPlayButtons = true)

    @Test
    fun `ACEのセットなし標準表示`() = captureListPane(Simulator.ACE, emptyList(), switchToPlayButtons = false)

    @Test
    fun `ACEのセットあり標準表示`() = captureListPane(Simulator.ACE, aceSets(), switchToPlayButtons = false)

    @Test
    fun `ACEのセットなし再生ボタン表示`() = captureListPane(Simulator.ACE, emptyList(), switchToPlayButtons = true)

    @Test
    fun `ACEのセットあり再生ボタン表示`() = captureListPane(Simulator.ACE, aceSets(), switchToPlayButtons = true)

    private fun lmuSets() =
        listOf(
            ManualReadoutSet(
                id = "set-1",
                name = "燃料とタイヤ",
                itemKeys =
                    listOf(
                        LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
                        LmuWindowsReadoutItemKey.TyreWear.Root.value,
                    ),
                hotkeyVirtualKeyCode = 0x41,
            ),
            ManualReadoutSet(id = "set-2"),
            ManualReadoutSet(
                id = "set-3",
                itemKeys = listOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value),
            ),
        )

    private fun gt7Sets() =
        listOf(
            ManualReadoutSet(
                id = "set-1",
                name = "燃料と周回数",
                itemKeys =
                    listOf(
                        Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value,
                        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value,
                    ),
                hotkeyVirtualKeyCode = 0x41,
            ),
            ManualReadoutSet(id = "set-2"),
            ManualReadoutSet(id = "set-3", itemKeys = listOf(Gt7Ps5ReadoutItemKey.MyBestLap.Root.value)),
        )

    private fun aceSets() =
        listOf(
            ManualReadoutSet(
                id = "set-1",
                name = "燃料とタイヤ",
                itemKeys =
                    listOf(
                        AceWindowsReadoutItemKey.RemainingFuel.Root.value,
                        AceWindowsReadoutItemKey.TyreTemperature.Root.value,
                    ),
                hotkeyVirtualKeyCode = 0x41,
            ),
            ManualReadoutSet(id = "set-2"),
            ManualReadoutSet(id = "set-3", itemKeys = listOf(AceWindowsReadoutItemKey.MyBestLap.Root.value)),
        )

    @OptIn(ExperimentalTestApi::class)
    private fun captureListPane(
        simulator: Simulator,
        sets: List<ManualReadoutSet>,
        switchToPlayButtons: Boolean,
    ) = composeScreenshotTest {
        showListPane(simulator, sets)
        if (switchToPlayButtons) {
            onNodeWithContentDescription("再生ボタン表示").performClick()
        }
        onRoot().captureRoboImage()
    }

    @OptIn(ExperimentalTestApi::class)
    private fun DesktopComposeUiTest.showListPane(
        simulator: Simulator,
        sets: List<ManualReadoutSet>,
    ) {
        setContent {
            var playButtonLayoutEnabled by remember { mutableStateOf(false) }
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                        ManualReadoutListPaneContent(
                            uiState =
                                ManualReadoutListUiState(
                                    playButtonLayoutEnabled = playButtonLayoutEnabled,
                                    canReadSets = true,
                                    sets = sets,
                                ),
                            simulatorId = simulator.id,
                            simulatorName = simulator.displayName,
                            onAddSet = {},
                            onReadSet = {},
                            onTogglePlayButtonLayout = { playButtonLayoutEnabled = !playButtonLayoutEnabled },
                            showHotkeyChip = true,
                        )
                    }
                }
            }
        }
    }

    private enum class Simulator(
        val id: String,
        val displayName: String,
    ) {
        LMU("lmu_windows", "LMU"),
        GT7("gt7_ps5", "GT7"),
        ACE("ace_windows", "ACE"),
    }
}
