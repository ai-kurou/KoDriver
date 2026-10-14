package kurou.kodriver.feature.manualreadoutsetdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.manualReadoutItemKeys
import org.junit.Test

class ManualReadoutSetDetailPaneScreenshotTest {
    @Test
    fun `項目あり`() = screenshot(listOf("energy"))

    @Test
    fun `項目なし`() = screenshot(emptyList())

    @Test
    fun `戻るボタンなし`() = screenshot(listOf("energy"), canNavigateBack = false)

    @Test
    fun `1ペインで戻るボタンを表示`() = screenshot(listOf("energy"), width = 360, height = 640)

    private fun screenshot(
        keys: List<String>,
        canNavigateBack: Boolean = true,
        width: Int = 1560,
        height: Int = 1080,
        selected: Boolean = false,
        itemTexts: Map<String, String> = emptyMap(),
    ) = composeScreenshotTest {
        setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(width.dp, height.dp)) {
                        ManualReadoutSetDetailPane(
                            uiState =
                                ManualReadoutSetDetailUiState(
                                    setNumber = 1,
                                    availableItemKeys = keys,
                                    selectedItemKeys = if (selected) keys else emptyList(),
                                    itemTexts = itemTexts,
                                ),
                            onToggleItem = {},
                            onDelete = {},
                            canNavigateBack = canNavigateBack,
                        )
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }

    @Test
    fun `削除ダイアログ`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                        ManualReadoutSetDeleteConfirmDialog({}, {})
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }

    @Test
    fun `選択項目の既定文言欄`() = screenshot(manualReadoutItemKeys(Simulator.LmuWindows.id), selected = true)

    @Test
    fun `編集文言と未知プレースホルダー警告`() {
        val keys = manualReadoutItemKeys(Simulator.LmuWindows.id)
        screenshot(keys, selected = true, itemTexts = mapOf(keys.single() to "残量{unknown}%"))
    }

    @Test
    fun `空欄の文言`() {
        val keys = manualReadoutItemKeys(Simulator.LmuWindows.id)
        screenshot(keys, selected = true, itemTexts = mapOf(keys.single() to ""))
    }
}
