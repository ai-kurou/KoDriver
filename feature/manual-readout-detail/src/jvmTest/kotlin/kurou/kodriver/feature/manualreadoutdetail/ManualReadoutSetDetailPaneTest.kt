package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutSetDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `項目選択時は既定文言の残量を埋めた完成文を表示し空欄では非表示にする`() {
        val key = lmuWindowsManualReadoutItemKeys().first()
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                    selectedItemKeys = listOf(key),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = {},
                    onDelete = {},
                    showHotkey = false,
                )
            }
        }
        rule
            .onNodeWithTag("manual_readout_preview_sentence")
            .assertExists()
            .assertTextEquals("バーチャルエネルギー残量は50パーセント")
        rule.runOnIdle { state.value = state.value.copy(itemTexts = mapOf(key to "")) }
        rule.onNodeWithTag("manual_readout_preview_sentence").assertDoesNotExist()
    }

    @Test
    fun `未選択時は完成文を表示しない`() {
        val key = lmuWindowsManualReadoutItemKeys().first()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState =
                        ManualReadoutSetDetailUiState(
                            simulatorId = Simulator.LmuWindows.id,
                            setNumber = 1,
                            availableItemKeys = listOf(key),
                        ),
                    onAddItem = {},
                    onDelete = {},
                    showHotkey = false,
                )
            }
        }
        rule.onNodeWithText("0 / 3").assertExists()
        rule.onNodeWithText("最大3項目まで追加できます").assertExists()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertDoesNotExist()
    }

    @Test
    fun `戻るボタンは戻れる場合だけ表示しクリックを通知する`() {
        val canNavigateBack = mutableStateOf(false)
        var backed = 0
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, setNumber = 1),
                    onAddItem = {},
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
    fun `未対応項目と見つからない状態を表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, setNumber = 1, isLoading = false),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(state.value, {}, {})
            }
        }
        rule.onNodeWithText("このシミュレーターで選べる項目はまだありません").assertExists()
        rule.runOnIdle {
            state.value =
                ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, isLoading = false)
        }
        rule.onNodeWithText("セットが見つかりません").assertExists()
        rule.onNodeWithContentDescription("セットを削除").assertDoesNotExist()
    }

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

    @Test
    fun `プレビュー再生を通知しTTS不可と空文言では無効にする`() {
        val key = lmuWindowsManualReadoutItemKeys().first()
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                    selectedItemKeys = listOf(key),
                    isTextToSpeechAvailable = true,
                ),
            )
        var clicked = 0
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = {},
                    onDelete = {},
                    onPreviewClick = { clicked++ },
                    showHotkey = false,
                )
            }
        }
        rule.onNodeWithText("試聴").assertIsEnabled().performClick()
        rule.runOnIdle {
            assertEquals(1, clicked)
            state.value = state.value.copy(isTextToSpeechAvailable = false)
        }
        rule.onNodeWithText("試聴").assertIsNotEnabled()
        rule.runOnIdle { state.value = state.value.copy(isTextToSpeechAvailable = true, itemTexts = mapOf(key to "")) }
        rule.onNodeWithText("試聴").assertIsNotEnabled()
        rule.runOnIdle { state.value = state.value.copy(itemTexts = emptyMap(), isPreviewing = true) }
        rule.onNodeWithText("試聴を停止").assertIsEnabled()
    }

    @Test
    fun `ドラッグ以外に上下移動のアクセシビリティ操作で並べ替えられる`() {
        val keys = listOf("A", "B", "C")
        val moves = mutableListOf<Pair<Int, Int>>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState =
                        ManualReadoutSetDetailUiState(
                            simulatorId = Simulator.LmuWindows.id,
                            setNumber = 1,
                            availableItemKeys = lmuWindowsManualReadoutItemKeys(),
                            selectedItemKeys = keys,
                        ),
                    onAddItem = {},
                    onDelete = {},
                    onMoveItem = { from, to -> moves.add(from to to) },
                    showHotkey = false,
                )
            }
        }
        val rows = rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.CustomActions)).fetchSemanticsNodes()
        val labels = rows.map { row -> row.config[SemanticsActions.CustomActions].map { it.label } }
        assertEquals(listOf(listOf("下へ移動"), listOf("上へ移動", "下へ移動"), listOf("上へ移動")), labels)
        rule.runOnIdle {
            rows.forEach { row ->
                row.config[SemanticsActions.CustomActions].forEach { assertEquals(true, it.action()) }
            }
        }
        assertEquals(listOf(0 to 1, 1 to 0, 1 to 2, 2 to 1), moves)
    }
}
