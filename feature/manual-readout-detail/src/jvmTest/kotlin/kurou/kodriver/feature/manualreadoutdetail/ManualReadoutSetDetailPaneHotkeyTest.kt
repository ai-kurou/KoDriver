package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutSetDetailPaneHotkeyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ホットキーの入力と解除と重複エラーと非表示を扱う`() {
        val state = mutableStateOf(ManualReadoutSetDetailUiState(simulatorId = Simulator.Gt7Ps5.id, setNumber = 1))
        val showHotkey = mutableStateOf(true)
        val assigned = mutableListOf<Int>()
        var cleared = 0
        val modifiers = mutableListOf<ManualReadoutHotkeyModifiers>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = {},
                    onDelete = {},
                    showHotkey = showHotkey.value,
                    onAssignHotkey = { assigned.add(it) },
                    onSelectModifiers = { modifiers.add(it) },
                    onClearHotkey = { cleared++ },
                )
            }
        }
        rule.onNodeWithTag("manual_readout_hotkey_modifiers").performClick()
        rule.onNodeWithTag("manual_readout_hotkey_modifiers_CTRL_SHIFT").performClick()
        rule.runOnIdle { assertEquals(listOf(ManualReadoutHotkeyModifiers.CTRL_SHIFT), modifiers) }
        rule.onNodeWithText("割り当てなし").assertExists()
        rule.onNodeWithText("割り当てを解除").assertIsNotEnabled()
        rule.onNodeWithTag("manual_readout_hotkey_key").performClick().performKeyInput {
            pressKey(Key.A)
            pressKey(Key.Zero)
            pressKey(Key.F1)
        }
        rule.runOnIdle {
            assertEquals(listOf(0x41, 0x30), assigned)
            state.value = state.value.copy(hotkeyVirtualKeyCode = 0x41)
        }
        rule.onNodeWithText("A").assertExists()
        rule.onNodeWithText("割り当てを解除").performClick()
        rule.runOnIdle {
            assertEquals(1, cleared)
            state.value =
                state.value.copy(hotkeyConflict = ManualReadoutHotkeyConflict.OtherSet(Simulator.Gt7Ps5.id, 2))
        }
        rule.onNodeWithText("Gran Turismo 7（PS5） のセット2で使用中のため割り当てできません").assertExists()
        rule.runOnIdle { showHotkey.value = false }
        rule.onNodeWithTag("manual_readout_hotkey_card").assertDoesNotExist()
    }

    @Test
    fun `割り当て済みの登録状態と競合失敗時の再試行だけ表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    hotkeyVirtualKeyCode = 0x41,
                ),
            )
        var retries = 0
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = {},
                    onDelete = {},
                    showHotkey = true,
                    onRetryHotkeyRegistration = { retries++ },
                )
            }
        }
        rule.onNodeWithText("再試行").assertDoesNotExist()
        val states =
            listOf(
                HotkeyRegistrationState.Registering to "登録中",
                HotkeyRegistrationState.Registered to "登録済み",
                HotkeyRegistrationState.Conflict to "他のアプリが使用中のため登録できません",
                HotkeyRegistrationState.Failed(5) to "登録に失敗しました（エラーコード: 5）",
            )
        states.forEach { (registration, text) ->
            rule.runOnIdle { state.value = state.value.copy(hotkeyRegistrationState = registration) }
            rule.onNodeWithText(text).assertExists()
            if (registration is HotkeyRegistrationState.Conflict || registration is HotkeyRegistrationState.Failed) {
                rule.onNodeWithText("再試行").performClick()
            } else {
                rule.onNodeWithText("再試行").assertDoesNotExist()
            }
        }
        rule.runOnIdle {
            assertEquals(2, retries)
            state.value = state.value.copy(hotkeyVirtualKeyCode = null)
        }
        rule.onNodeWithText("登録に失敗しました（エラーコード: 5）").assertDoesNotExist()
        rule.onNodeWithText("再試行").assertDoesNotExist()
    }
}
