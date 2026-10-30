package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.acewindows.model.aceWindowsManualReadoutItemKeys
import kurou.kodriver.domain.gt7ps5.model.gt7Ps5ManualReadoutItemKeys
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.MANUAL_READOUT_SET_ITEM_MAX
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutSetDetailPaneAddSheetTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `LMUの追加候補はカテゴリ見出しの下に項目を順番に表示する`() {
        rule.assertPickerCategories(
            itemKeys = lmuWindowsManualReadoutItemKeys(),
            expectedTexts =
                listOf(
                    "読み上げる項目を選択",
                    "あと3項目追加できます",
                    "バーチャルエネルギー",
                    "2",
                    "バーチャルエネルギー残量",
                    "バーチャルエネルギーの残り周回数",
                    "燃料",
                    "2",
                    "残り燃料",
                    "燃料の残り周回数",
                    "タイヤ",
                    "3",
                    "タイヤ摩耗",
                    "タイヤ温度",
                    "タイヤ摩耗の残り周回数",
                    "ブレーキ",
                    "2",
                    "ブレーキ残量",
                    "ブレーキ温度",
                    "ラップ",
                    "3",
                    "自己ベストラップ",
                    "クラス内前方車とのギャップ",
                    "クラス内後方車とのギャップ",
                ),
        )
    }

    @Test
    fun `ACEの追加候補はカテゴリ見出しの下に項目を順番に表示する`() {
        rule.assertPickerCategories(
            itemKeys = aceWindowsManualReadoutItemKeys(),
            expectedTexts =
                listOf(
                    "読み上げる項目を選択",
                    "あと3項目追加できます",
                    "燃料",
                    "2",
                    "残り燃料",
                    "残り燃料の周回数",
                    "ラップ",
                    "1",
                    "自己ベストラップ",
                    "タイヤ",
                    "1",
                    "タイヤ温度",
                ),
        )
    }

    @Test
    fun `GT7の追加候補はカテゴリ見出しの下に項目を順番に表示する`() {
        rule.assertPickerCategories(
            itemKeys = gt7Ps5ManualReadoutItemKeys(),
            expectedTexts =
                listOf(
                    "読み上げる項目を選択",
                    "あと3項目追加できます",
                    "燃料",
                    "2",
                    "残り燃料",
                    "残り燃料の周回数",
                    "タイヤ",
                    "1",
                    "タイヤ温度",
                    "ラップ",
                    "1",
                    "自己ベストラップ",
                ),
        )
    }

    @Test
    fun `選択数に応じてシートの残り枠を表示する`() {
        val selectedCount = mutableIntStateOf(0)
        rule.setContent {
            KoDriverTheme {
                ManualReadoutItemPickerSheetContent(
                    itemKeys = gt7Ps5ManualReadoutItemKeys(),
                    selectedCount = selectedCount.intValue,
                    onSelect = {},
                )
            }
        }
        rule.onNodeWithText("読み上げる項目を選択").assertExists()
        for (count in 0..MANUAL_READOUT_SET_ITEM_MAX) {
            rule.runOnIdle { selectedCount.intValue = count }
            rule.onNodeWithText("あと${MANUAL_READOUT_SET_ITEM_MAX - count}項目追加できます").assertExists()
        }
    }

    @Test
    fun `選択済み項目数を追加シートの残り枠に反映する`() {
        val keys = gt7Ps5ManualReadoutItemKeys()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState =
                        ManualReadoutSetDetailUiState(
                            simulatorId = Simulator.Gt7Ps5.id,
                            setNumber = 1,
                            availableItemKeys = keys,
                            selectedItemKeys = keys.take(2),
                        ),
                    onAddItem = {},
                    onDelete = {},
                    showHotkey = false,
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("読み上げる項目を選択").assertExists()
        rule.onNodeWithText("あと1項目追加できます").assertExists()
    }

    @Test
    fun `上限では追加を無効化する`() {
        val keys = List(MANUAL_READOUT_SET_ITEM_MAX) { "item$it" }
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState =
                        ManualReadoutSetDetailUiState(
                            simulatorId = Simulator.LmuWindows.id,
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
        rule.onNodeWithText("3 / 3").assertExists()
        rule
            .onNodeWithText("最大3項目に達しました。追加するには、選択済みの項目を削除してください。")
            .assertExists()
    }

    @Test
    fun `シートを閉じて再選択し削除確認のキャンセルと確定を通知する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 2,
                    availableItemKeys = lmuWindowsManualReadoutItemKeys(),
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
        rule.onNodeWithText("バーチャルエネルギー残量").assertDoesNotExist()
        rule.onNodeWithText("試聴").assertDoesNotExist()
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("バーチャルエネルギー").assertExists()
        rule.onNodeWithText("燃料").assertExists()
        rule.onNodeWithText("タイヤ").assertExists()
        rule.onNodeWithText("ブレーキ").assertExists()
        rule.onNodeWithText("ラップ").assertExists()
        rule.onNodeWithText("自己ベストラップ").assertExists()
        rule.onNodeWithText("バーチャルエネルギーの残り周回数").assertExists()
        rule.onNodeWithText("タイヤ摩耗の残り周回数").assertExists()
        rule.onNodeWithText("燃料の残り周回数").assertExists()
        rule.onNodeWithText("クラス内前方車とのギャップ").assertExists()
        rule.onNodeWithText("クラス内後方車とのギャップ").assertExists()
        rule.onNodeWithText("ブレーキ残量").assertExists()
        rule.onNodeWithText("ブレーキ温度").assertExists()
        rule.onNodeWithText("タイヤ温度").assertExists()
        rule.onNodeWithText("タイヤ摩耗").assertExists()
        rule.onNodeWithText("キャンセル").assertDoesNotExist()
        rule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss))
            .performSemanticsAction(SemanticsActions.Dismiss)
        rule.mainClock.advanceTimeBy(500)
        rule.onNodeWithText("バーチャルエネルギー残量").assertDoesNotExist()
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("タイヤ摩耗").performScrollTo().performClick()
        rule.runOnIdle {
            assertEquals(listOf(LmuWindowsReadoutItemKey.TyreWear.Root.value), state.value.selectedItemKeys)
        }
        rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss)).assertDoesNotExist()
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
}
