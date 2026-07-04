@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherthemedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ThemeMode
import org.junit.Rule
import org.junit.Test

class OtherThemeDetailDialogContentScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `システムに従うを選択中`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                        OtherThemeDetailDialogContent(
                            uiState = OtherThemeDetailUiState(pendingThemeMode = ThemeMode.SYSTEM),
                        )
                    }
                }
            }
        }
        rule.onNode(isDialog()).captureRoboImage()
    }

    @Test
    fun `ダークを選択中`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                        OtherThemeDetailDialogContent(
                            uiState = OtherThemeDetailUiState(pendingThemeMode = ThemeMode.DARK),
                        )
                    }
                }
            }
        }
        rule.onNode(isDialog()).captureRoboImage()
    }
}
