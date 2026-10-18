package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.MANUAL_READOUT_SET_ITEM_MAX
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.manualReadoutItemKeys
import org.junit.Test

class ManualReadoutSetDetailPaneScreenshotTest {
    @Test
    fun `セット名のインライン編集`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                        ManualReadoutSetDetailPane(
                            uiState = ManualReadoutSetDetailUiState(setNumber = 1, name = "決勝用"),
                            onAddItem = {},
                            onDelete = {},
                            showHotkey = false,
                        )
                    }
                }
            }
            onNodeWithContentDescription("セット名を編集").performClick()
            onRoot().captureRoboImage()
        }

    @Test
    fun `GT7の残り燃料と周回数の文言`() = screenshot(manualReadoutItemKeys(Simulator.Gt7Ps5.id), selected = true)

    @Test
    fun `項目追加ダイアログ`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                        ManualReadoutItemPickerDialog(manualReadoutItemKeys(Simulator.LmuWindows.id), {}, {})
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }

    @Test
    fun `項目あり`() = screenshot(manualReadoutItemKeys(Simulator.LmuWindows.id))

    @Test
    fun `項目なし`() = screenshot(emptyList())

    @Test
    fun `戻るボタンなし`() = screenshot(manualReadoutItemKeys(Simulator.LmuWindows.id), canNavigateBack = false)

    @Test
    fun `1ペインで戻るボタンを表示`() = screenshot(manualReadoutItemKeys(Simulator.LmuWindows.id), size = DpSize(360.dp, 640.dp))

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
    fun `タイヤ温度の文言欄と連結プレビュー`() =
        screenshot(
            listOf(
                LmuWindowsReadoutItemKey.TyreTemperature.Root.value,
                LmuWindowsReadoutItemKey.BrakeWear.Root.value,
                LmuWindowsReadoutItemKey.TyreWear.Root.value,
            ),
            selected = true,
        )

    @Test
    fun `ブレーキ温度の文言欄と連結プレビュー`() =
        screenshot(
            listOf(
                LmuWindowsReadoutItemKey.BrakeTemperature.Root.value,
                LmuWindowsReadoutItemKey.BrakeWear.Root.value,
                LmuWindowsReadoutItemKey.TyreWear.Root.value,
            ),
            selected = true,
        )

    @Test
    fun `選択項目の既定文言欄`() = screenshot(manualReadoutItemKeys(Simulator.LmuWindows.id), selected = true)

    @Test
    fun `編集文言と未知プレースホルダー警告`() {
        val keys = manualReadoutItemKeys(Simulator.LmuWindows.id)
        screenshot(keys, selected = true, itemTexts = mapOf(keys.first() to "残量{unknown}%"))
    }

    @Test
    fun `空欄の文言`() {
        val keys = manualReadoutItemKeys(Simulator.LmuWindows.id)
        screenshot(keys, selected = true, itemTexts = keys.associateWith { "" })
    }

    @Test
    fun `ホットキー未割り当て`() = screenshot(emptyList(), hotkey = null to null)

    @Test
    fun `ホットキー割り当て済み`() = screenshot(emptyList(), hotkey = 0x41 to null)

    @Test
    fun `ホットキー他セット重複`() =
        screenshot(
            emptyList(),
            hotkey =
                0x30 to ManualReadoutHotkeyConflict.OtherSet(Simulator.Gt7Ps5.id, 2),
        )

    @Test
    fun `ホットキーデバッグ重複`() = screenshot(emptyList(), hotkey = null to ManualReadoutHotkeyConflict.DebugHotkey)

    @Test
    fun `ホットキー登録競合と再試行`() =
        screenshot(emptyList(), hotkey = 0x41 to null, registration = HotkeyRegistrationState.Conflict)
}

class ManualReadoutSetDetailPaneAceScreenshotTest {
    @Test
    fun `ACEの残り燃料の文言`() = screenshot(manualReadoutItemKeys(Simulator.AceWindows.id), selected = true)

    @Test
    fun `ACEのタイヤ温度の文言欄と連結プレビュー`() =
        screenshot(
            listOf(
                AceWindowsReadoutItemKey.TyreTemperature.Root.value,
                AceWindowsReadoutItemKey.RemainingFuel.Root.value,
                AceWindowsReadoutItemKey.MyBestLap.Root.value,
            ),
            selected = true,
        )

    @Test
    fun `ACEの項目追加ダイアログ`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                        ManualReadoutItemPickerDialog(manualReadoutItemKeys(Simulator.AceWindows.id), {}, {})
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }
}

private fun screenshot(
    keys: List<String>,
    canNavigateBack: Boolean = true,
    size: DpSize = DpSize(1560.dp, 1080.dp),
    selected: Boolean = false,
    itemTexts: Map<String, String> = emptyMap(),
    hotkey: Pair<Int?, ManualReadoutHotkeyConflict?>? = null,
    registration: HotkeyRegistrationState = HotkeyRegistrationState.Unsupported,
) = composeScreenshotTest {
    setContent {
        KoDriverTheme {
            Surface {
                Box(modifier = Modifier.requiredSize(size.width, size.height)) {
                    ManualReadoutSetDetailPane(
                        uiState =
                            ManualReadoutSetDetailUiState(
                                setNumber = 1,
                                availableItemKeys = keys,
                                selectedItemKeys =
                                    if (selected) keys.take(MANUAL_READOUT_SET_ITEM_MAX) else emptyList(),
                                itemTexts = itemTexts,
                                hotkeyVirtualKeyCode = hotkey?.first,
                                hotkeyConflict = hotkey?.second,
                                hotkeyRegistrationState = registration,
                            ),
                        showHotkey = hotkey != null,
                        onAddItem = {},
                        onDelete = {},
                        canNavigateBack = canNavigateBack,
                    )
                }
            }
        }
    }
    onRoot().captureRoboImage()
}
