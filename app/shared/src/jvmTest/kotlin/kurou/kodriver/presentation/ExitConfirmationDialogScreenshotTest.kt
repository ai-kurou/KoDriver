@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class ExitConfirmationDialogScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    ExitConfirmationDialog(
                        onDismiss = {},
                        onConfirm = {},
                    )
                }
            }
        }
        rule.onNode(isDialog()).captureRoboImage()
    }

    @Test
    fun `次から表示しないにチェック済み`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    ExitConfirmationDialog(
                        onDismiss = {},
                        onConfirm = {},
                    )
                }
            }
        }
        rule.onNodeWithText("今後表示しない").performClick()
        rule.waitForIdle()
        rule.onNode(isDialog()).captureRoboImage()
    }
}
