package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutListPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `読み上げ可能な一覧のボタンは項目の有無に関係なく対応するセットIDを渡す`() {
        val requestedSets = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState =
                        ManualReadoutListUiState(
                            canReadSets = true,
                            sets =
                                listOf(
                                    ManualReadoutSet(id = "empty-set"),
                                    ManualReadoutSet(id = "filled-set", itemKeys = listOf("SESSION")),
                                ),
                        ),
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = { requestedSets.add(it) },
                )
            }
        }

        rule.onNodeWithContentDescription("セット 1 を読み上げ").assertIsEnabled().performClick()
        rule.onNodeWithContentDescription("セット 2 を読み上げ").assertIsEnabled().performClick()
        rule.runOnIdle { assertEquals(listOf("empty-set", "filled-set"), requestedSets) }
    }

    @Test
    fun `複数の保存済みセットのタイトルと項目数と無効な読み上げボタンを表示する`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState =
                        ManualReadoutListUiState(
                            sets =
                                listOf(
                                    ManualReadoutSet(id = "set-1"),
                                    ManualReadoutSet(id = "set-2", itemKeys = listOf("SESSION", "FLAG")),
                                ),
                        ),
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                )
            }
        }

        rule.onNodeWithText("手動読み上げについて").assertIsDisplayed()
        rule.onNodeWithText("LMUの手動読み上げセットはまだありません").assertDoesNotExist()
        rule.onNodeWithText("セット 1").assertIsDisplayed()
        rule.onNodeWithText("項目なし").assertIsDisplayed()
        rule.onNodeWithText("セット 2").assertIsDisplayed()
        rule.onNodeWithText("2項目").assertIsDisplayed()
        rule.onNodeWithContentDescription("セット 1 を読み上げ").assertIsDisplayed().assertIsNotEnabled()
        rule.onNodeWithContentDescription("セット 2 を読み上げ").assertIsDisplayed().assertIsNotEnabled()
        rule.onNodeWithContentDescription("セットを追加").assertIsDisplayed()
    }

    @Test
    fun `選択中のシミュレーター名を含む空状態で追加FABをクリックするとコールバックを呼ぶ`() {
        var addSetCount = 0
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState = ManualReadoutListUiState(),
                    simulatorName = "LMU",
                    onAddSet = { addSetCount++ },
                    onReadSet = {},
                )
            }
        }

        rule.onNodeWithText("手動読み上げについて").assertIsDisplayed()
        rule.onNodeWithText("LMUの手動読み上げセットはまだありません").assertIsDisplayed()
        rule.onNodeWithContentDescription("セットを追加").assertIsDisplayed().performClick()
        rule.runOnIdle { assertEquals(1, addSetCount) }
    }

    @Test
    fun `シミュレーター名を変更すると空状態の文言を更新する`() {
        val simulatorName = mutableStateOf("LMU")
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState = ManualReadoutListUiState(),
                    simulatorName = simulatorName.value,
                    onAddSet = {},
                    onReadSet = {},
                )
            }
        }

        rule.onNodeWithText("LMUの手動読み上げセットはまだありません").assertIsDisplayed()
        rule.runOnIdle { simulatorName.value = "ACE" }
        rule.onNodeWithText("ACEの手動読み上げセットはまだありません").assertIsDisplayed()
        rule.onNodeWithText("LMUの手動読み上げセットはまだありません").assertDoesNotExist()
    }

    @Test
    fun `ヘルプを開くと手動読み上げの説明を表示する`() {
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                    ManualReadoutListPaneContent(
                        uiState = ManualReadoutListUiState(),
                        simulatorName = "LMU",
                        onAddSet = {},
                        onReadSet = {},
                    )
                }
            }
        }

        rule.onNodeWithContentDescription("手動読み上げの説明を表示").performClick()

        rule.onNodeWithText("読み上げ").assertIsDisplayed()
        rule.onNodeWithText("順番待ち").assertIsDisplayed()
        rule.onNodeWithText("データがないとき").assertIsDisplayed()
        rule
            .onNodeWithText("自動読み上げのON/OFFに関係なく読み上げます。自動読み上げの「読み上げ済み」の状態は変わりません。")
            .performScrollTo()
            .assertIsDisplayed()
        rule
            .onNodeWithText("読み上げ中のときや自動読み上げと重なったときは、キューの最後に並んで順番に読み上げます。")
            .performScrollTo()
            .assertIsDisplayed()
        rule
            .onNodeWithText("読み上げる値がない項目は「データがありません」と読み上げます。")
            .performScrollTo()
            .assertIsDisplayed()
        rule.onNodeWithText("長押しで削除").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("セットを長押しすると、確認してから削除できます。").performScrollTo().assertIsDisplayed()
        // ホットキーの説明は Windows デスクトップでのみ表示するため、実行OSに合わせて検証する。
        if (isManualReadoutHotkeySupported()) {
            rule.onNodeWithText("ホットキー").performScrollTo().assertIsDisplayed()
        } else {
            rule.onNodeWithText("ホットキー").assertDoesNotExist()
        }
    }

    @Test
    fun `ホットキーの説明を有効にすると表示する`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutHintSheetContent(showHotkeyHint = true)
            }
        }

        rule.onNodeWithText("ホットキー").performScrollTo().assertIsDisplayed()
        rule
            .onNodeWithText("選んだ修飾キーと数字または英字キーの組み合わせで、セットを読み上げられます。")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `ホットキーの説明を無効にすると表示しない`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutHintSheetContent(showHotkeyHint = false)
            }
        }

        rule.onNodeWithText("読み上げ").assertIsDisplayed()
        rule.onNodeWithText("順番待ち").assertIsDisplayed()
        rule.onNodeWithText("データがないとき").assertIsDisplayed()
        rule.onNodeWithText("ホットキー").assertDoesNotExist()
        rule
            .onNodeWithText("選んだ修飾キーと数字または英字キーの組み合わせで、セットを読み上げられます。")
            .assertDoesNotExist()
    }

    @Test
    fun `行タップは詳細を開き読み上げボタンは詳細を開かない`() {
        val selected = mutableListOf<String>()
        val requested = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState = ManualReadoutListUiState(canReadSets = true, sets = listOf(ManualReadoutSet("set-1"))),
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = { requested.add(it) },
                    onSetClick = { selected.add(it) },
                )
            }
        }
        rule.onNodeWithContentDescription("セット 1 を読み上げ").performClick()
        rule.runOnIdle { assertEquals(emptyList(), selected) }
        rule.onNodeWithText("セット 1").performClick()
        rule.runOnIdle {
            assertEquals(listOf("set-1"), selected)
            assertEquals(listOf("set-1"), requested)
        }
    }

    @Test
    fun `長押しは詳細を開かず削除メニューから確認したセットだけを削除する`() {
        val deleted = mutableListOf<String>()
        val selected = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState =
                        ManualReadoutListUiState(
                            sets = listOf(ManualReadoutSet("first"), ManualReadoutSet("second")),
                        ),
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                    onSetClick = { selected.add(it) },
                    onDeleteSet = { deleted.add(it) },
                )
            }
        }
        rule.onNodeWithText("セット 2").performTouchInput { longClick() }
        rule.onNodeWithText("削除").assertIsDisplayed().performClick()
        rule.onNodeWithText("セットを削除しますか？").assertIsDisplayed()
        rule.onNodeWithText("この操作は取り消せません。").assertIsDisplayed()
        rule.runOnIdle {
            assertEquals(emptyList(), selected)
            assertEquals(emptyList(), deleted)
        }
        rule.onNodeWithText("キャンセル").performClick()
        rule.onNodeWithText("セットを削除しますか？").assertDoesNotExist()
        rule.runOnIdle { assertEquals(emptyList(), deleted) }
        rule.onNodeWithText("セット 2").performTouchInput { longClick() }
        rule.onNodeWithText("削除").performClick()
        rule.onNodeWithText("削除").performClick()
        rule.onNodeWithText("セットを削除しますか？").assertDoesNotExist()
        rule.onNodeWithText("削除").assertDoesNotExist()
        rule.runOnIdle { assertEquals(listOf("second"), deleted) }
    }
}
