package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.TextRange
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.acewindows.model.aceWindowsManualReadoutItemKeys
import kurou.kodriver.domain.acewindows.model.aceWindowsManualReadoutItemTextDefault
import kurou.kodriver.domain.gt7ps5.model.gt7Ps5ManualReadoutItemKeys
import kurou.kodriver.domain.gt7ps5.model.gt7Ps5ManualReadoutItemTextDefault
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemTextDefault
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@Suppress("TooManyFunctions")
class ManualReadoutSetDetailPaneTextEditTest {
    init {
        registerAllManualReadoutSimulators()
    }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `バーチャルエネルギーの残り周回数の文言欄は周回数の挿入と警告とリセットと文字数上限を扱う`() {
        val key = LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value
        val defaultText = lmuWindowsManualReadoutItemTextDefault(key)
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                ),
            )
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
        rule.onNodeWithText("バーチャルエネルギーの残り周回数").performScrollTo().performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNodeWithText("{laps}は残りエネルギーで走れる周回数に置き換わります。").assertExists()
        rule.onNodeWithText("未対応のプレースホルダー: {laps}（そのまま読み上げます）").assertDoesNotExist()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{laps}を挿入").performClick()
        rule.onNodeWithText("{laps}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あと{percent}")
        rule.onNodeWithText("未対応のプレースホルダー: {percent}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{laps}を挿入").performClick()
        rule.onNodeWithText("30/30").assertExists()
        rule.onNodeWithContentDescription("バーチャルエネルギーの残り周回数を削除").performClick()
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        rule.runOnIdle {
            assertEquals(
                listOf(
                    key to "",
                    key to "{laps}",
                    key to "あと{percent}",
                    key to defaultText,
                    key to "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH),
                ),
                saved,
            )
        }
    }

    @Test
    fun `タイヤ摩耗の残り周回数の文言欄は周回数の挿入と警告とリセットと文字数上限を扱う`() {
        val key = LmuWindowsReadoutItemKey.PitTiming.TyreWear.value
        val defaultText = lmuWindowsManualReadoutItemTextDefault(key)
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                ),
            )
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
        rule.onNodeWithText("タイヤ摩耗の残り周回数").performScrollTo().performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNodeWithText("{laps}は残りタイヤ摩耗で走れる周回数に置き換わります。").assertExists()
        rule.onNodeWithText("未対応のプレースホルダー: {laps}（そのまま読み上げます）").assertDoesNotExist()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{laps}を挿入").performClick()
        rule.onNodeWithText("{laps}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あと{percent}")
        rule.onNodeWithText("未対応のプレースホルダー: {percent}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{laps}を挿入").performClick()
        rule.onNodeWithText("30/30").assertExists()
        rule.onNodeWithContentDescription("タイヤ摩耗の残り周回数を削除").performClick()
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        rule.runOnIdle {
            assertEquals(
                listOf(
                    key to "",
                    key to "{laps}",
                    key to "あと{percent}",
                    key to defaultText,
                    key to "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH),
                ),
                saved,
            )
        }
    }

    @Test
    fun `燃料の残り周回数の文言欄は周回数の挿入と警告とリセットと文字数上限を扱う`() {
        val key = LmuWindowsReadoutItemKey.RemainingFuel.Laps.value
        val defaultText = lmuWindowsManualReadoutItemTextDefault(key)
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                ),
            )
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
        rule.onNodeWithText("燃料の残り周回数").performScrollTo().performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNodeWithText("{laps}は残り燃料で走れる周回数に置き換わります。").assertExists()
        rule.onNodeWithText("未対応のプレースホルダー: {laps}（そのまま読み上げます）").assertDoesNotExist()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{laps}を挿入").performClick()
        rule.onNodeWithText("{laps}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あと{percent}")
        rule.onNodeWithText("未対応のプレースホルダー: {percent}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{laps}を挿入").performClick()
        rule.onNodeWithText("30/30").assertExists()
        rule.onNodeWithContentDescription("燃料の残り周回数を削除").performClick()
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        rule.runOnIdle {
            assertEquals(
                listOf(
                    key to "",
                    key to "{laps}",
                    key to "あと{percent}",
                    key to defaultText,
                    key to "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH),
                ),
                saved,
            )
        }
    }

    @Test
    fun `クラス内ギャップの文言欄は秒数の挿入と警告とリセットと文字数上限を扱う`() {
        val key = LmuWindowsReadoutItemKey.ClassCarGap.Behind.value
        val defaultText = lmuWindowsManualReadoutItemTextDefault(key)
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                ),
            )
        val saved = mutableListOf<Pair<String, String>>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
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
        rule.onNodeWithText("クラス内後方車とのギャップ").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        val classCarGapHint =
            "{seconds}は同じクラスの前後の車との差（秒、小数1桁）に置き換わります。" +
                "前に車がいないときは「クラス内先頭です」、" +
                "後ろに車がいないときは「クラス内最後尾です」と読み上げます。"
        rule.onNodeWithText(classCarGapHint).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{seconds}を挿入").performClick()
        rule.onNodeWithText("{seconds}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あと{percent}")
        rule.onNodeWithText("未対応のプレースホルダー: {percent}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{seconds}を挿入").performClick()
        rule.onNodeWithText("30/30").assertExists()
        rule.onNodeWithContentDescription("クラス内後方車とのギャップを削除").performClick()
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        rule.runOnIdle {
            assertEquals(
                listOf(
                    key to "",
                    key to "{seconds}",
                    key to "あと{percent}",
                    key to defaultText,
                    key to "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH),
                ),
                saved,
            )
        }
    }

    @Test
    fun `自己ベストラップの文言欄はラップタイムの挿入と警告とリセットと文字数上限を扱う`() {
        val key = LmuWindowsReadoutItemKey.MyBestLap.Root.value
        val defaultText = lmuWindowsManualReadoutItemTextDefault(key)
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                ),
            )
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
        rule.onNodeWithText("自己ベストラップ").performScrollTo().performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNodeWithText("{laptime} はセッションの自己ベストラップタイムに置き換わります").assertExists()
        rule.onNodeWithText("未対応のプレースホルダー: {laptime}（そのまま読み上げます）").assertDoesNotExist()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{laptime}を挿入").performClick()
        rule.onNodeWithText("{laptime}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("記録{percent}")
        rule.onNodeWithText("未対応のプレースホルダー: {percent}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{laptime}を挿入").performClick()
        rule.onNodeWithText("30/30").assertExists()
        rule.onNodeWithContentDescription("自己ベストラップを削除").performClick()
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        rule.runOnIdle {
            assertEquals(
                listOf(
                    key to "",
                    key to "{laptime}",
                    key to "記録{percent}",
                    key to defaultText,
                    key to "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH),
                ),
                saved,
            )
        }
    }

    @Test
    fun `GT7の自己ベストラップの文言欄はラップタイムの挿入と警告とリセットと文字数上限を扱う`() {
        val key = Gt7Ps5ReadoutItemKey.MyBestLap.Root.value
        val defaultText = gt7Ps5ManualReadoutItemTextDefault(key)
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.Gt7Ps5.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                ),
            )
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
        rule.onNodeWithText("自己ベストラップ").performScrollTo().performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNodeWithText("{laptime} はセッションの自己ベストラップタイムに置き換わります").assertExists()
        rule.onNodeWithText("未対応のプレースホルダー: {laptime}（そのまま読み上げます）").assertDoesNotExist()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{laptime}を挿入").performClick()
        rule.onNodeWithText("{laptime}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("記録{percent}")
        rule.onNodeWithText("未対応のプレースホルダー: {percent}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{laptime}を挿入").performClick()
        rule.onNodeWithText("30/30").assertExists()
        rule.onNodeWithContentDescription("自己ベストラップを削除").performClick()
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        rule.runOnIdle {
            assertEquals(
                listOf(
                    key to "",
                    key to "{laptime}",
                    key to "記録{percent}",
                    key to defaultText,
                    key to "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH),
                ),
                saved,
            )
        }
    }

    @Test
    fun `LMUの燃料残量を追加して文言を編集し既定文言に戻せる`() {
        val key = LmuWindowsReadoutItemKey.RemainingFuel.Root.value
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = lmuWindowsManualReadoutItemKeys(),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onItemTextChanged = { item, text ->
                        state.value = state.value.copy(itemTexts = mapOf(item to text))
                    },
                    onDelete = {},
                    showHotkey = false,
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("残り燃料").performScrollTo().performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("燃料は残り50パーセント")
        rule.onNodeWithText("{percent} は現在の残量（%）に置き換わります").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("残り{percent}%")
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("残り50%")
        assertEquals("残り{percent}%", state.value.itemTexts[key])
        rule.onNode(hasSetTextAction()).performTextReplacement("残り{other}%")
        rule.onNodeWithText("未対応のプレースホルダー: {other}（そのまま読み上げます）").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("{percent}を挿入").performClick()
        rule.onNodeWithText("{percent}").assertExists()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("50")
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("燃料は残り50パーセント")
    }

    @Test
    fun `ACEの残り燃料を追加して文言を編集し既定文言に戻せる`() {
        val key = AceWindowsReadoutItemKey.RemainingFuel.Root.value
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.AceWindows.id,
                    setNumber = 1,
                    availableItemKeys = aceWindowsManualReadoutItemKeys(),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onItemTextChanged = { item, text ->
                        state.value = state.value.copy(itemTexts = mapOf(item to text))
                    },
                    onDelete = {},
                    showHotkey = false,
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("残り燃料").performScrollTo().performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("燃料は残り50パーセント")
        rule.onNodeWithText("{percent} は現在の残量（%）に置き換わります").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("残り{percent}%")
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("残り50%")
        assertEquals("残り{percent}%", state.value.itemTexts[key])
        rule.onNode(hasSetTextAction()).performTextReplacement("残り{other}%")
        rule.onNodeWithText("未対応のプレースホルダー: {other}（そのまま読み上げます）").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("{percent}を挿入").performClick()
        rule.onNodeWithText("{percent}").assertExists()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("50")
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("燃料は残り50パーセント")
    }

    @Test
    fun `GT7の残り燃料を追加して文言を編集し既定文言に戻せる`() {
        val key = Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.Gt7Ps5.id,
                    setNumber = 1,
                    availableItemKeys = gt7Ps5ManualReadoutItemKeys(),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onItemTextChanged = { item, text ->
                        state.value = state.value.copy(itemTexts = mapOf(item to text))
                    },
                    onDelete = {},
                    showHotkey = false,
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("残り燃料").performScrollTo().performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("燃料は残り50パーセント")
        rule.onNodeWithText("{percent} は現在の残量（%）に置き換わります").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("残り{percent}%")
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("残り50%")
        assertEquals("残り{percent}%", state.value.itemTexts[key])
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("燃料は残り50パーセント")
    }

    @Test
    fun `GT7の周回数の文言欄は周回数の挿入と警告とリセットと文字数上限を扱う`() {
        val key = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value
        val defaultText = gt7Ps5ManualReadoutItemTextDefault(key)
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.Gt7Ps5.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                ),
            )
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
        rule.onNodeWithText("残り燃料の周回数").performScrollTo().performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNodeWithText("{laps}は残り燃料で走れる周回数に置き換わります。").assertExists()
        rule.onNodeWithText("未対応のプレースホルダー: {laps}（そのまま読み上げます）").assertDoesNotExist()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{laps}を挿入").performClick()
        rule.onNodeWithText("{laps}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("温度{other}℃")
        rule.onNodeWithText("未対応のプレースホルダー: {other}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{laps}を挿入").performClick()
        rule.onNodeWithText("30/30").assertExists()
        rule.onNodeWithContentDescription("残り燃料の周回数を削除").performClick()
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        rule.runOnIdle {
            assertEquals(
                listOf(
                    key to "",
                    key to "{laps}",
                    key to "温度{other}℃",
                    key to defaultText,
                    key to "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH),
                ),
                saved,
            )
        }
    }

    @Test
    fun `ACEのタイヤ温度の文言欄は摂氏の挿入と警告とリセットと文字数上限を扱う`() {
        val key = AceWindowsReadoutItemKey.TyreTemperature.Root.value
        val defaultText = aceWindowsManualReadoutItemTextDefault(key)
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.AceWindows.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                ),
            )
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
        rule.onNodeWithText("タイヤ温度").performScrollTo().performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNodeWithText("{celsius} は現在の項目の最高温度（℃）に置き換わります").assertExists()
        rule.onNodeWithText("未対応のプレースホルダー: {celsius}（そのまま読み上げます）").assertDoesNotExist()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{celsius}を挿入").performClick()
        rule.onNodeWithText("{celsius}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("温度{other}℃")
        rule.onNodeWithText("未対応のプレースホルダー: {other}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{celsius}を挿入").performClick()
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
    fun `GT7のタイヤ温度の文言欄は摂氏の挿入と警告とリセットと文字数上限を扱う`() {
        val key = Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value
        val defaultText = gt7Ps5ManualReadoutItemTextDefault(key)
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.Gt7Ps5.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                ),
            )
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
        rule.onNodeWithText("タイヤ温度").performScrollTo().performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNodeWithText("{celsius} は現在の項目の最高温度（℃）に置き換わります").assertExists()
        rule.onNodeWithText("未対応のプレースホルダー: {celsius}（そのまま読み上げます）").assertDoesNotExist()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{celsius}を挿入").performClick()
        rule.onNodeWithText("{celsius}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("温度{other}℃")
        rule.onNodeWithText("未対応のプレースホルダー: {other}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{celsius}を挿入").performClick()
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
    fun `タイヤ温度の文言欄は摂氏の挿入と警告とリセットと文字数上限を扱う`() {
        val key = LmuWindowsReadoutItemKey.TyreTemperature.Root.value
        val defaultText = lmuWindowsManualReadoutItemTextDefault(key)
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                ),
            )
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
        rule.onNodeWithText("タイヤ温度").performScrollTo().performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNodeWithText("{celsius} は現在の項目の最高温度（℃）に置き換わります").assertExists()
        rule.onNodeWithText("未対応のプレースホルダー: {celsius}（そのまま読み上げます）").assertDoesNotExist()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{celsius}を挿入").performClick()
        rule.onNodeWithText("{celsius}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("温度{other}℃")
        rule.onNodeWithText("未対応のプレースホルダー: {other}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{celsius}を挿入").performClick()
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
        val defaultText = lmuWindowsManualReadoutItemTextDefault(key)
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                ),
            )
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
        rule.onNodeWithText("ブレーキ温度").performScrollTo().performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNodeWithText("未対応のプレースホルダー: {celsius}（そのまま読み上げます）").assertDoesNotExist()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{celsius}を挿入").performClick()
        rule.onNodeWithText("{celsius}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("温度{other}℃")
        rule.onNodeWithText("未対応のプレースホルダー: {other}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{celsius}を挿入").performClick()
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
        val key = lmuWindowsManualReadoutItemKeys().first()
        val defaultText = lmuWindowsManualReadoutItemTextDefault(key)
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = listOf(key),
                ),
            )
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
        rule.onNodeWithText("バーチャルエネルギー残量").performScrollTo().performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithText("{percent}を挿入").performClick()
        rule.onNodeWithText("{percent}").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("残量{other}%")
        rule.onNodeWithText("未対応のプレースホルダー: {other}（そのまま読み上げます）").assertExists()
        rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
        rule.onNodeWithText(defaultText).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onNodeWithText("{percent}を挿入").performClick()
        rule.onNodeWithText("30/30").assertExists()
        rule.onNodeWithContentDescription("バーチャルエネルギー残量を削除").performClick()
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
    fun `プレースホルダーはカーソル位置に挿入し選択範囲を置換する`() {
        val text = mutableStateOf("前後")
        rule.setContent {
            KoDriverTheme {
                ManualReadoutItemTextField(
                    text.value,
                    "既定",
                    { text.value = it },
                    LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                    Simulator.LmuWindows.id,
                )
            }
        }
        rule.onNode(hasSetTextAction()).performTextInputSelection(TextRange(1))
        rule.onNodeWithText("{percent}を挿入").performClick()
        rule.runOnIdle { assertEquals("前{percent}後", text.value) }
        assertEquals(
            TextRange(10),
            rule.onNode(hasSetTextAction()).fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange],
        )
        rule.onNode(hasSetTextAction()).performTextInputSelection(TextRange(1, 10))
        rule.onNodeWithText("{percent}を挿入").performClick()
        rule.runOnIdle { assertEquals("前{percent}後", text.value) }
        rule.onNode(hasSetTextAction()).performTextInputSelection(TextRange(0, 11))
        rule.onNodeWithText("{percent}を挿入").performClick()
        rule.runOnIdle { assertEquals("{percent}", text.value) }
    }

    @Test
    fun `文字数上限では挿入せず選択範囲の置換で収まる場合は挿入する`() {
        val full = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH)
        val text = mutableStateOf(full)
        rule.setContent {
            KoDriverTheme {
                ManualReadoutItemTextField(
                    text.value,
                    "既定",
                    { text.value = it },
                    LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                    Simulator.LmuWindows.id,
                )
            }
        }
        rule.onNodeWithText("{percent}を挿入").performClick()
        rule.runOnIdle { assertEquals(full, text.value) }
        rule.onNode(hasSetTextAction()).performTextInputSelection(TextRange(0, 9))
        rule.onNodeWithText("{percent}を挿入").performClick()
        rule.runOnIdle { assertEquals("{percent}" + "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH - 9), text.value) }
    }
}
