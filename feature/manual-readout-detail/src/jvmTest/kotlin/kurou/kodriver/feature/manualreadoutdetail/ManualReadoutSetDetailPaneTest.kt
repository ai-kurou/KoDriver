package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.text.TextRange
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.MANUAL_READOUT_SET_ITEM_MAX
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
    fun `タイヤ温度を追加すると既定文言とプレビューを表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    setNumber = 1,
                    availableItemKeys = manualReadoutItemKeys(Simulator.LmuWindows.id),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = {},
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("タイヤ温度").performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("タイヤ温度90℃")
    }

    @Test
    fun `ブレーキ温度を追加すると既定文言とプレビューを表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    setNumber = 1,
                    availableItemKeys = manualReadoutItemKeys(Simulator.LmuWindows.id),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = {},
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("ブレーキ温度").performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("ブレーキ温度500℃")
    }

    @Test
    fun `ブレーキ残量を追加すると既定文言とプレビューを表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    setNumber = 1,
                    availableItemKeys = manualReadoutItemKeys(Simulator.LmuWindows.id),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = {},
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("ブレーキ残量").performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("ブレーキ残量50%")
    }

    @Test
    fun `項目選択時は既定文言の残量を埋めた完成文を表示し空欄では非表示にする`() {
        val key = manualReadoutItemKeys(Simulator.LmuWindows.id).first()
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
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
            .assertTextEquals("バーチャルエナジー残量は50パーセント")
        rule.runOnIdle { state.value = state.value.copy(itemTexts = mapOf(key to "")) }
        rule.onNodeWithTag("manual_readout_preview_sentence").assertDoesNotExist()
    }

    @Test
    fun `未選択時は完成文を表示しない`() {
        val key = manualReadoutItemKeys(Simulator.LmuWindows.id).first()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = ManualReadoutSetDetailUiState(setNumber = 1, availableItemKeys = listOf(key)),
                    onAddItem = {},
                    onDelete = {},
                    showHotkey = false,
                )
            }
        }
        rule.onNodeWithTag("manual_readout_preview_sentence").assertDoesNotExist()
    }

    @Test
    fun `上限では追加を無効化する`() {
        val keys = List(MANUAL_READOUT_SET_ITEM_MAX) { "item$it" }
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState =
                        ManualReadoutSetDetailUiState(
                            setNumber = 1,
                            availableItemKeys = keys + "extra",
                            selectedItemKeys = keys,
                            itemTexts = keys.zip(listOf("先頭", " ", "末尾")).toMap(),
                        ),
                    onAddItem = {},
                    onDelete = {},
                    showHotkey = false,
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").assertIsNotEnabled()
        rule.onNodeWithText("1セットに追加できる項目は3つまでです").assertExists()
    }

    @Test
    fun `戻るボタンは戻れる場合だけ表示しクリックを通知する`() {
        val canNavigateBack = mutableStateOf(false)
        var backed = 0
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = ManualReadoutSetDetailUiState(setNumber = 1),
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
    fun `選択と削除確認のキャンセルと確定を通知する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    setNumber = 2,
                    availableItemKeys = manualReadoutItemKeys(Simulator.LmuWindows.id),
                ),
            )
        var deleted = 0
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = { deleted++ },
                )
            }
        }
        rule.onNodeWithText("バーチャルエナジー残量").assertDoesNotExist()
        rule.onNodeWithText("試聴").assertDoesNotExist()
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("エネルギー").assertExists()
        rule.onNodeWithText("タイヤ").assertExists()
        rule.onNodeWithText("ブレーキ").assertExists()
        rule.onNodeWithText("ブレーキ残量").assertExists()
        rule.onNodeWithText("ブレーキ温度").assertExists()
        rule.onNodeWithText("タイヤ温度").assertExists()
        rule.onNodeWithText("タイヤ摩耗").assertExists()
        rule.onNodeWithText("キャンセル").performClick()
        rule.onNodeWithText("バーチャルエナジー残量").assertDoesNotExist()
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("タイヤ摩耗").performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("タイヤ摩耗残り50%")
        rule.onNodeWithText("＋ 項目を追加").assertIsEnabled()
        rule.onNodeWithText("試聴").assertExists()
        rule.onNodeWithContentDescription("セットを削除").performClick()
        rule.onNodeWithText("キャンセル").performClick()
        rule.runOnIdle { assertEquals(0, deleted) }
        rule.onNodeWithContentDescription("セットを削除").performClick()
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
        rule.onNodeWithContentDescription("セットを削除").assertDoesNotExist()
    }

    @Test
    fun `タイヤ温度の文言欄は摂氏の挿入と警告とリセットと文字数上限を扱う`() {
        val key = LmuWindowsReadoutItemKey.TyreTemperature.Root.value
        val defaultText = manualReadoutItemTextDefault(key)
        val state = mutableStateOf(ManualReadoutSetDetailUiState(setNumber = 1, availableItemKeys = listOf(key)))
        val saved = mutableListOf<Pair<String, String>>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = {
                        state.value =
                            state.value.copy(
                                selectedItemKeys = listOf(it),
                            )
                    },
                    onRemoveItem = { state.value = state.value.copy(selectedItemKeys = emptyList()) },
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
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("タイヤ温度").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNodeWithText("{celsius} は現在の項目の最高温度（℃）に置き換わります").assertExists()
        rule.onNodeWithText("未対応のプレースホルダー: {celsius}（そのまま読み上げます）").assertDoesNotExist()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{celsius} を挿入").performClick()
        rule.onNodeWithText("{celsius}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("温度{other}℃")
        rule.onNodeWithText("未対応のプレースホルダー: {other}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{celsius} を挿入").performClick()
        rule.onNodeWithText("30/30").assertExists()
        rule.onNodeWithContentDescription("タイヤ温度を削除").performClick()
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        rule.runOnIdle {
            assertEquals(
                listOf(
                    key to "",
                    key to "{celsius}",
                    key to "温度{other}℃",
                    key to defaultText,
                    key to "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH),
                ),
                saved,
            )
        }
    }

    @Test
    fun `温度の文言欄は摂氏の挿入と警告とリセットと文字数上限を扱う`() {
        val key = LmuWindowsReadoutItemKey.BrakeTemperature.Root.value
        val defaultText = manualReadoutItemTextDefault(key)
        val state = mutableStateOf(ManualReadoutSetDetailUiState(setNumber = 1, availableItemKeys = listOf(key)))
        val saved = mutableListOf<Pair<String, String>>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = {
                        state.value =
                            state.value.copy(
                                selectedItemKeys = listOf(it),
                            )
                    },
                    onRemoveItem = { state.value = state.value.copy(selectedItemKeys = emptyList()) },
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
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("ブレーキ温度").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNodeWithText("未対応のプレースホルダー: {celsius}（そのまま読み上げます）").assertDoesNotExist()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{celsius} を挿入").performClick()
        rule.onNodeWithText("{celsius}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("温度{other}℃")
        rule.onNodeWithText("未対応のプレースホルダー: {other}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{celsius} を挿入").performClick()
        rule.onNodeWithText("30/30").assertExists()
        rule.onNodeWithContentDescription("ブレーキ温度を削除").performClick()
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        rule.runOnIdle {
            assertEquals(
                listOf(
                    key to "",
                    key to "{celsius}",
                    key to "温度{other}℃",
                    key to defaultText,
                    key to "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH),
                ),
                saved,
            )
        }
    }

    @Test
    fun `選択時だけ文言欄を表示し入力と挿入と警告とリセットと文字数上限を扱う`() {
        val key = manualReadoutItemKeys(Simulator.LmuWindows.id).first()
        val defaultText = manualReadoutItemTextDefault(key)
        val state = mutableStateOf(ManualReadoutSetDetailUiState(setNumber = 1, availableItemKeys = listOf(key)))
        val saved = mutableListOf<Pair<String, String>>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = {
                        state.value =
                            state.value.copy(
                                selectedItemKeys = listOf(it),
                            )
                    },
                    onRemoveItem = { state.value = state.value.copy(selectedItemKeys = emptyList()) },
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
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("バーチャルエナジー残量").performClick()
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
        rule.onNodeWithText("{percent} を挿入").performClick()
        rule.onNodeWithText("30/30").assertExists()
        rule.onNodeWithContentDescription("バーチャルエナジー残量を削除").performClick()
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
        val key = manualReadoutItemKeys(Simulator.LmuWindows.id).first()
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
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
    fun `プレースホルダーはカーソル位置に挿入し選択範囲を置換する`() {
        val text = mutableStateOf("前後")
        rule.setContent {
            KoDriverTheme {
                ManualReadoutItemTextField(text.value, "既定", { text.value = it })
            }
        }
        rule.onNode(hasSetTextAction()).performTextInputSelection(TextRange(1))
        rule.onNodeWithText("{percent} を挿入").performClick()
        rule.runOnIdle { assertEquals("前{percent}後", text.value) }
        assertEquals(
            TextRange(10),
            rule.onNode(hasSetTextAction()).fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange],
        )
        rule.onNode(hasSetTextAction()).performTextInputSelection(TextRange(1, 10))
        rule.onNodeWithText("{percent} を挿入").performClick()
        rule.runOnIdle { assertEquals("前{percent}後", text.value) }
        rule.onNode(hasSetTextAction()).performTextInputSelection(TextRange(0, 11))
        rule.onNodeWithText("{percent} を挿入").performClick()
        rule.runOnIdle { assertEquals("{percent}", text.value) }
    }

    @Test
    fun `文字数上限では挿入せず選択範囲の置換で収まる場合は挿入する`() {
        val full = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH)
        val text = mutableStateOf(full)
        rule.setContent {
            KoDriverTheme {
                ManualReadoutItemTextField(text.value, "既定", { text.value = it })
            }
        }
        rule.onNodeWithText("{percent} を挿入").performClick()
        rule.runOnIdle { assertEquals(full, text.value) }
        rule.onNode(hasSetTextAction()).performTextInputSelection(TextRange(0, 9))
        rule.onNodeWithText("{percent} を挿入").performClick()
        rule.runOnIdle { assertEquals("{percent}" + "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH - 9), text.value) }
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
                            setNumber = 1,
                            availableItemKeys = manualReadoutItemKeys(Simulator.LmuWindows.id),
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
