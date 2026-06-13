package kurou.kodriver.feature.otherdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class OtherDetailPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `戻るボタンを表示`() {
        captureOtherDetailPane(canNavigateBack = true)
    }

    @Test
    fun `戻るボタンを非表示`() {
        captureOtherDetailPane(canNavigateBack = false)
    }

    private fun captureOtherDetailPane(canNavigateBack: Boolean) {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherDetailPane(
                            title = "ライセンス",
                            canNavigateBack = canNavigateBack,
                            onBack = {},
                            content = {},
                        )
                    }
                }
            }
        }

        rule.onRoot().captureRoboImage()
    }
}
