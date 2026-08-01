package kurou.kodriver.feature.desktopsplash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class DesktopSplashErrorDialogScreenshotTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `起動エラー`() {
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(600.dp, 500.dp)) {
                    DesktopSplashErrorDialog(
                        message = "モジュールの初期化に失敗しました",
                        onConfirm = {},
                    )
                }
            }
        }
        rule.onNode(isDialog()).captureRoboImage()
    }
}
