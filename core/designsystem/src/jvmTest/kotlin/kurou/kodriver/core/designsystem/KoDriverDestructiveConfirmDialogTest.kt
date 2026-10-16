package kurou.kodriver.core.designsystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class KoDriverDestructiveConfirmDialogTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `指定した文言を表示し確認ボタンはonConfirmだけを呼ぶ`() {
        var confirmCount = 0
        var dismissCount = 0
        rule.setContent {
            KoDriverTheme {
                KoDriverDestructiveConfirmDialog(
                    title = "ログを削除",
                    body = "選択したログを削除します。",
                    confirmLabel = "削除",
                    dismissLabel = "キャンセル",
                    onConfirm = { confirmCount++ },
                    onDismiss = { dismissCount++ },
                )
            }
        }

        rule.onNodeWithText("ログを削除").assertIsDisplayed()
        rule.onNodeWithText("選択したログを削除します。").assertIsDisplayed()
        rule.onNodeWithText("キャンセル").assertIsDisplayed()
        rule.onNodeWithText("削除").performClick()

        assertEquals(1, confirmCount)
        assertEquals(0, dismissCount)
    }

    @Test
    fun `キャンセルボタンはonDismissだけを呼ぶ`() {
        var confirmCount = 0
        var dismissCount = 0
        rule.setContent {
            KoDriverTheme {
                KoDriverDestructiveConfirmDialog(
                    title = "設定をリセット",
                    body = "Assetto Corsa EVO の設定をリセットします。",
                    confirmLabel = "リセット",
                    dismissLabel = "キャンセル",
                    onConfirm = { confirmCount++ },
                    onDismiss = { dismissCount++ },
                )
            }
        }

        rule.onNodeWithText("キャンセル").performClick()

        assertEquals(0, confirmCount)
        assertEquals(1, dismissCount)
    }
}
