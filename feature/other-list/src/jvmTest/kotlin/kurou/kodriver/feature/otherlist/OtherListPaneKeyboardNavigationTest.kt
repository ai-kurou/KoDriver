package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class OtherListPaneKeyboardNavigationTest {
    @get:Rule
    val rule = createComposeRule()

    private fun setPane(
        items: List<OtherListItemType>,
        height: Int = 900,
        appVersionLabel: String = "",
        appVersion: String = "",
    ) {
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(480.dp, height.dp)) {
                    OtherListPane(
                        uiState =
                            OtherListUiState(
                                items = items,
                                appVersionLabel = appVersionLabel,
                                appVersion = appVersion,
                            ),
                        onItemClick = {},
                        onOverlayVisibleChange = {},
                        onKeepScreenOnChange = {},
                        onDynamicColorEnabledChange = {},
                        onHapticFeedbackEnabledChange = {},
                        onStartupEnabledChange = {},
                    )
                }
            }
        }
    }

    private fun press(
        text: String,
        key: Key,
    ) {
        rule.onNodeWithText(text).performKeyInput { pressKey(key) }
    }

    @Test
    fun `下キーで同じセクション内の次の行へフォーカスが移る`() {
        setPane(listOf(OtherListItemType.ShowLogs, OtherListItemType.DeleteAllLogs))
        rule.onNodeWithText("ログを表示").requestFocus()

        press("ログを表示", Key.DirectionDown)

        rule.onNodeWithText("ログをすべて削除").assertIsFocused()
    }

    @Test
    fun `上キーで前の行へフォーカスが移る`() {
        setPane(listOf(OtherListItemType.ShowLogs, OtherListItemType.DeleteAllLogs))
        rule.onNodeWithText("ログをすべて削除").requestFocus()

        press("ログをすべて削除", Key.DirectionUp)

        rule.onNodeWithText("ログを表示").assertIsFocused()
    }

    @Test
    fun `先頭の行で上キーを押してもフォーカスは動かない`() {
        setPane(listOf(OtherListItemType.ShowLogs, OtherListItemType.DeleteAllLogs))
        rule.onNodeWithText("ログを表示").requestFocus()

        press("ログを表示", Key.DirectionUp)

        rule.onNodeWithText("ログを表示").assertIsFocused()
    }

    @Test
    fun `末尾の行で下キーを押してもフォーカスは動かない`() {
        setPane(listOf(OtherListItemType.ShowLogs, OtherListItemType.DeleteAllLogs))
        rule.onNodeWithText("ログをすべて削除").requestFocus()

        press("ログをすべて削除", Key.DirectionDown)

        rule.onNodeWithText("ログをすべて削除").assertIsFocused()
    }

    @Test
    fun `セクションの境界と見出しをまたいで上下キーで移動できる`() {
        setPane(listOf(OtherListItemType.Volume, OtherListItemType.Theme, OtherListItemType.ShowLogs))
        rule.onNodeWithText("音量").requestFocus()

        press("音量", Key.DirectionDown)
        rule.onNodeWithText("テーマ").assertIsFocused()

        press("テーマ", Key.DirectionDown)
        rule.onNodeWithText("ログを表示").assertIsFocused()

        press("ログを表示", Key.DirectionUp)
        rule.onNodeWithText("テーマ").assertIsFocused()
    }

    @Test
    fun `スイッチにフォーカスがあっても下キーで次の行へ移る`() {
        setPane(listOf(OtherListItemType.KeepScreenOn, OtherListItemType.Theme))
        rule.onNode(isToggleable()).requestFocus()

        rule.onNode(isToggleable()).performKeyInput { pressKey(Key.DirectionDown) }

        rule.onNodeWithText("テーマ").assertIsFocused()
    }

    @Test
    fun `画面外の行へも下キーの連打でフォーカスが移る`() {
        val items =
            listOf(
                OtherListItemType.Volume,
                OtherListItemType.Theme,
                OtherListItemType.KeepScreenOn,
                OtherListItemType.OverlayVisible,
                OtherListItemType.ShowLogs,
                OtherListItemType.DeleteAllLogs,
            )
        setPane(items, height = 240)
        rule.onNodeWithText("音量").requestFocus()

        // 画面外へ出た行は合成されないため、テキストで探さずフォーカス中の行へキーを送る
        repeat(items.size - 1) {
            rule.onRoot().performKeyInput { pressKey(Key.DirectionDown) }
        }

        rule.onNodeWithText("ログをすべて削除").assertIsFocused()
    }

    @Test
    fun `Alt Ctrl Meta Shift付きの下キーでは行を移動しない`() {
        setPane(listOf(OtherListItemType.ShowLogs, OtherListItemType.DeleteAllLogs))
        rule.onNodeWithText("ログを表示").requestFocus()

        listOf(Key.AltLeft, Key.CtrlLeft, Key.MetaLeft, Key.ShiftLeft).forEach { modifier ->
            rule.onNodeWithText("ログを表示").performKeyInput {
                keyDown(modifier)
                pressKey(Key.DirectionDown)
                keyUp(modifier)
            }
        }

        rule.onNodeWithText("ログを表示").assertIsFocused()
    }

    @Test
    fun `アプリバージョン行へ下キーで移動し、情報セクションの末尾から次の行へも移れる`() {
        setPane(
            listOf(OtherListItemType.License, OtherListItemType.ShowLogs),
            appVersionLabel = "アプリバージョン",
            appVersion = "1.2.3",
        )
        rule.onNodeWithText("ライセンス").requestFocus()

        press("ライセンス", Key.DirectionDown)
        rule.onNodeWithText("アプリバージョン").assertIsFocused()

        press("アプリバージョン", Key.DirectionDown)
        rule.onNodeWithText("ログを表示").assertIsFocused()

        press("ログを表示", Key.DirectionUp)
        rule.onNodeWithText("アプリバージョン").assertIsFocused()
    }
}
