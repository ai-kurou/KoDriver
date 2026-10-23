package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutListPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `名前ありは名前を名前なしは番号を表示し読み上げボタンにも使う`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState =
                        ManualReadoutListUiState(
                            sets = listOf(ManualReadoutSet("named", name = "燃料用"), ManualReadoutSet("unnamed")),
                        ),
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                )
            }
        }
        rule.onNodeWithText("燃料用").assertIsDisplayed()
        rule.onNodeWithText("セット 2").assertIsDisplayed()
        rule.onNodeWithContentDescription("燃料用 を読み上げ").assertIsDisplayed()
        rule.onNodeWithContentDescription("セット 2 を読み上げ").assertIsDisplayed()
    }

    @Test
    fun `対応環境では割り当て済みと未設定のチップを表示する`() {
        showHotkeySets(showHotkeyChip = true)

        rule.onNodeWithText("Ctrl+Shift+A", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("未設定", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithContentDescription("セット 1 を読み上げ").assertIsDisplayed()
    }

    @Test
    fun `非対応環境では割り当て済みも未設定もチップを表示しない`() {
        showHotkeySets(showHotkeyChip = false)

        rule.onNodeWithText("Ctrl+Shift+A", useUnmergedTree = true).assertDoesNotExist()
        rule.onNodeWithText("未設定", useUnmergedTree = true).assertDoesNotExist()
        rule.onNodeWithText("セット 1").assertIsDisplayed()
        rule.onNodeWithText("セット 2").assertIsDisplayed()
    }

    private fun showHotkeySets(showHotkeyChip: Boolean) {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState =
                        ManualReadoutListUiState(
                            sets =
                                listOf(
                                    ManualReadoutSet(
                                        id = "assigned",
                                        hotkeyModifiers = ManualReadoutHotkeyModifiers.CTRL_SHIFT,
                                        hotkeyVirtualKeyCode = 0x41,
                                    ),
                                    ManualReadoutSet(id = "unassigned"),
                                ),
                        ),
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                    showHotkeyChip = showHotkeyChip,
                )
            }
        }
    }

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
    fun `LMUとGT7とACEに合わせて空状態の文言を更新する`() {
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

        listOf("LMU", "GT7", "ACE").forEach { name ->
            rule.runOnIdle { simulatorName.value = name }
            rule.onNodeWithText("${name}の手動読み上げセットはまだありません").assertIsDisplayed()
        }
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
        val uiState =
            mutableStateOf(
                ManualReadoutListUiState(
                    sets = listOf(ManualReadoutSet("first"), ManualReadoutSet("second")),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState = uiState.value,
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                    onSetClick = { selected.add(it) },
                    onDeleteSet = { setId ->
                        deleted.add(setId)
                        uiState.value = uiState.value.copy(sets = uiState.value.sets.filterNot { it.id == setId })
                    },
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
        rule.mainClock.advanceTimeBy(500)
        rule.onNodeWithText("セット 2").performTouchInput { longClick() }
        rule.onNodeWithText("削除").performClick()
        val deleteActions = rule.onAllNodesWithText("削除")
        deleteActions.assertCountEquals(1)
        deleteActions[0].performClick()
        rule.onNodeWithText("セットを削除しますか？").assertDoesNotExist()
        rule.mainClock.advanceTimeBy(500)
        rule.onNodeWithText("削除").assertDoesNotExist()
        rule.runOnIdle { assertEquals(listOf("second"), deleted) }
    }

    @Test
    fun `長押し位置の近くに削除メニューを表示し画面端では範囲内に収める`() {
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                    ManualReadoutListPaneContent(
                        uiState = ManualReadoutListUiState(sets = listOf(ManualReadoutSet("set-1"))),
                        simulatorName = "LMU",
                        onAddSet = {},
                        onReadSet = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        val row = rule.onNodeWithText("セット 1")
        val rowBounds = row.fetchSemanticsNode().boundsInRoot
        val pressOffset = Offset(rowBounds.width / 2, rowBounds.height / 2)
        val pressPosition = rowBounds.topLeft + pressOffset
        val tolerance = 16f
        val menuItem = rule.onNodeWithText("削除")
        row.performTouchInput { longClick(pressOffset) }
        menuItem.assertIsDisplayed()
        val standardMenuBounds = menuItem.fetchSemanticsNode().boundsInRoot
        rule.runOnIdle {
            assertEquals(true, standardMenuBounds.left >= pressPosition.x - tolerance)
            assertEquals(true, standardMenuBounds.left <= pressPosition.x + tolerance)
            assertEquals(true, standardMenuBounds.top >= pressPosition.y - tolerance)
            assertEquals(true, standardMenuBounds.top <= pressPosition.y + tolerance)
            assertEquals(true, standardMenuBounds.width <= 280f)
            assertEquals(true, standardMenuBounds.center.y >= rowBounds.center.y)
            assertEquals(true, standardMenuBounds.left >= 0f)
            assertEquals(true, standardMenuBounds.right <= 360f)
            assertEquals(true, standardMenuBounds.top >= 0f)
            assertEquals(true, standardMenuBounds.bottom <= 640f)
        }
    }

    @Test
    fun `読み上げボタンのタップ後にセマンティクスで長押しすると行中央にメニューを表示する`() {
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                    ManualReadoutListPaneContent(
                        uiState = ManualReadoutListUiState(sets = listOf(ManualReadoutSet("set-1"))),
                        simulatorName = "LMU",
                        onAddSet = {},
                        onReadSet = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        val row = rule.onNodeWithText("セット 1")
        val rowBounds = row.fetchSemanticsNode().boundsInRoot
        rule.onNodeWithContentDescription("セット 1 を読み上げ").performClick()
        row.performSemanticsAction(SemanticsActions.OnLongClick) { it() }

        val menuItem = rule.onNodeWithText("削除")
        menuItem.assertIsDisplayed()
        val menuBounds = menuItem.fetchSemanticsNode().boundsInRoot
        val rowCenter = rowBounds.center
        rule.runOnIdle {
            assertEquals(true, menuBounds.left >= rowCenter.x - 16f)
            assertEquals(true, menuBounds.left <= rowCenter.x + 16f)
            assertEquals(true, menuBounds.top >= rowCenter.y - 16f)
            assertEquals(true, menuBounds.top <= rowCenter.y + 16f)
        }
    }

    @Test
    fun `メニュー位置を左右上下端とレイアウト方向に合わせて補正する`() {
        val window = IntSize(360, 640)
        val anchorBounds = IntRect(0, 0, 360, 640)
        val menuSize = IntSize(100, 48)
        val lowerRightPosition =
            ManualReadoutMenuPositionProvider(Offset(350f, 630f)).calculatePosition(
                anchorBounds = anchorBounds,
                windowSize = window,
                layoutDirection = LayoutDirection.Ltr,
                popupContentSize = menuSize,
            )
        assertEquals(IntOffset(260, 582), lowerRightPosition)

        val upperLeftRtlPosition =
            ManualReadoutMenuPositionProvider(Offset(10f, 10f)).calculatePosition(
                anchorBounds = anchorBounds,
                windowSize = window,
                layoutDirection = LayoutDirection.Rtl,
                popupContentSize = menuSize,
            )
        assertEquals(IntOffset(0, 10), upperLeftRtlPosition)

        val semanticLongPressFallback =
            ManualReadoutMenuPositionProvider(Offset.Unspecified).calculatePosition(
                anchorBounds = IntRect(20, 30, 220, 130),
                windowSize = window,
                layoutDirection = LayoutDirection.Ltr,
                popupContentSize = menuSize,
            )
        assertEquals(IntOffset(120, 80), semanticLongPressFallback)
    }
}
