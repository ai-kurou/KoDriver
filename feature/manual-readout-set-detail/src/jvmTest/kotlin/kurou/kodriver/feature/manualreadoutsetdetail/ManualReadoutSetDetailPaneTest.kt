package kurou.kodriver.feature.manualreadoutsetdetail

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.manualReadoutItemKeys
import kurou.kodriver.domain.model.manualReadoutItemTextDefault
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutSetDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `戻るボタンは戻れる場合だけ表示しクリックを通知する`() {
        val canNavigateBack = mutableStateOf(false)
        var backed = 0
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = ManualReadoutSetDetailUiState(setNumber = 1),
                    onToggleItem = {},
                    onDelete = {},
                    showHotkey = false,
                    canNavigateBack = canNavigateBack.value,
                    onBack = { backed++ },
                )
            }
        }
        rule.onNodeWithContentDescription("戻る").assertDoesNotExist()
        rule.runOnIdle { canNavigateBack.value = true }
        rule.onNodeWithContentDescription("戻る").assertExists().performClick()
        rule.runOnIdle { assertEquals(1, backed) }
    }

    @Test
    fun `選択と削除確認のキャンセルと確定を通知する`() {
        val state = mutableStateOf(ManualReadoutSetDetailUiState(setNumber = 2, availableItemKeys = listOf("energy")))
        var deleted = 0
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onToggleItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = { deleted++ },
                )
            }
        }
        rule.onNode(isToggleable()).assertIsOff().performClick()
        rule.onNode(isToggleable()).assertIsOn()
        rule.onNodeWithText("セットを削除").performClick()
        rule.onNodeWithText("キャンセル").performClick()
        rule.runOnIdle { assertEquals(0, deleted) }
        rule.onNodeWithText("セットを削除").performClick()
        rule.onNodeWithText("削除").performClick()
        rule.runOnIdle { assertEquals(1, deleted) }
    }

    @Test
    fun `未対応項目と見つからない状態を表示する`() {
        val state = mutableStateOf(ManualReadoutSetDetailUiState(setNumber = 1, isLoading = false))
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(state.value, {}, {})
            }
        }
        rule.onNodeWithText("このシミュレーターで選べる項目はまだありません").assertExists()
        rule.runOnIdle { state.value = ManualReadoutSetDetailUiState(isLoading = false) }
        rule.onNodeWithText("セットが見つかりません").assertExists()
        rule.onNodeWithText("セットを削除").assertDoesNotExist()
    }

    @Test
    fun `選択時だけ文言欄を表示し入力と挿入と警告とリセットと文字数上限を扱う`() {
        val key = manualReadoutItemKeys(Simulator.LmuWindows.id).single()
        val defaultText = manualReadoutItemTextDefault(key)
        val state = mutableStateOf(ManualReadoutSetDetailUiState(setNumber = 1, availableItemKeys = listOf(key)))
        val saved = mutableListOf<Pair<String, String>>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onToggleItem = {
                        state.value =
                            state.value.copy(
                                selectedItemKeys = if (it in state.value.selectedItemKeys) emptyList() else listOf(it),
                            )
                    },
                    onDelete = {},
                    showHotkey = false,
                    onItemTextChanged = { itemKey, text ->
                        saved.add(itemKey to text)
                        state.value = state.value.copy(itemTexts = state.value.itemTexts + (itemKey to text.trim()))
                    },
                )
            }
        }
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        rule.onNode(isToggleable()).performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{percent} を挿入").performClick()
        rule.onNodeWithText("{percent}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("残量{other}%")
        rule.onNodeWithText("未対応のプレースホルダー: {other}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{percent} を挿入").assertIsNotEnabled()
        rule.onNodeWithText("30/30").assertExists()
        rule.onNode(isToggleable()).performClick()
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        rule.runOnIdle {
            assertEquals(
                listOf(
                    key to "",
                    key to "{percent}",
                    key to "残量{other}%",
                    key to defaultText,
                    key to "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH),
                ),
                saved,
            )
        }
    }

    @Test
    fun `ホットキーの入力と解除と重複エラーと非表示を扱う`() {
        val state = mutableStateOf(ManualReadoutSetDetailUiState(setNumber = 1))
        val showHotkey = mutableStateOf(true)
        val assigned = mutableListOf<Int>()
        var cleared = 0
        val modifiers = mutableListOf<ManualReadoutHotkeyModifiers>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onToggleItem = {},
                    onDelete = {},
                    showHotkey = showHotkey.value,
                    onAssignHotkey = { assigned.add(it) },
                    onSelectModifiers = { modifiers.add(it) },
                    onClearHotkey = { cleared++ },
                )
            }
        }
        rule.onNodeWithTag("manual_readout_hotkey_modifiers").performClick()
        rule.onNodeWithTag("manual_readout_hotkey_modifiers_CTRL_ALT").performClick()
        rule.runOnIdle { assertEquals(listOf(ManualReadoutHotkeyModifiers.CTRL_ALT), modifiers) }
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
        rule.runOnIdle { state.value = state.value.copy(hotkeyConflict = ManualReadoutHotkeyConflict.DebugHotkey) }
        rule.onNodeWithText("デバッグ用の読み上げホットキーで使用中のため割り当てできません").assertExists()
        rule.runOnIdle { showHotkey.value = false }
        rule.onNodeWithTag("manual_readout_hotkey_card").assertDoesNotExist()
    }

    @Test
    fun `割り当て済みの登録状態と競合失敗時の再試行だけ表示する`() {
        val state = mutableStateOf(ManualReadoutSetDetailUiState(setNumber = 1, hotkeyVirtualKeyCode = 0x41))
        var retries = 0
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onToggleItem = {},
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
