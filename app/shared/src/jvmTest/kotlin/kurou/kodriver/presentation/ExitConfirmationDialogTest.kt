package kurou.kodriver.presentation

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExitConfirmationDialogTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `キャンセルボタンを押すとonDismissが呼ばれる`() {
        var dismissed = false
        rule.setContent {
            KoDriverTheme {
                ExitConfirmationDialog(
                    onDismiss = { dismissed = true },
                    onConfirm = {},
                )
            }
        }

        rule.onNode(hasText("キャンセル")).performClick()
        rule.waitForIdle()

        assertTrue(dismissed)
    }

    @Test
    fun `今後表示しないをオフのまま終了するとdoNotShowAgainがfalseでonConfirmが呼ばれる`() {
        var confirmedWith: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                ExitConfirmationDialog(
                    onDismiss = {},
                    onConfirm = { confirmedWith = it },
                )
            }
        }

        rule.onNode(hasText("終了")).performClick()
        rule.waitForIdle()

        assertEquals(false, confirmedWith)
    }

    @Test
    fun `今後表示しないをオンにして終了するとdoNotShowAgainがtrueでonConfirmが呼ばれる`() {
        var confirmedWith: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                ExitConfirmationDialog(
                    onDismiss = {},
                    onConfirm = { confirmedWith = it },
                )
            }
        }

        rule.onNode(hasText("今後表示しない")).performClick()
        rule.waitForIdle()
        rule.onNode(hasText("終了")).performClick()
        rule.waitForIdle()

        assertEquals(true, confirmedWith)
    }

    @Test
    fun `キャンセルを押してもonConfirmは呼ばれない`() {
        var confirmedWith: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                ExitConfirmationDialog(
                    onDismiss = {},
                    onConfirm = { confirmedWith = it },
                )
            }
        }

        rule.onNode(hasText("キャンセル")).performClick()
        rule.waitForIdle()

        assertNull(confirmedWith)
    }

    @Test
    fun `終了ボタンを押してもonDismissは呼ばれない`() {
        var dismissed = false
        rule.setContent {
            KoDriverTheme {
                ExitConfirmationDialog(
                    onDismiss = { dismissed = true },
                    onConfirm = {},
                )
            }
        }

        rule.onNode(hasText("終了")).performClick()
        rule.waitForIdle()

        assertFalse(dismissed)
    }
}
