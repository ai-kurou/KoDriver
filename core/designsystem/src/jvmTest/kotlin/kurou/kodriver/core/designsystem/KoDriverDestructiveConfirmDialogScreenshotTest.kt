package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test

class KoDriverDestructiveConfirmDialogScreenshotTest {
    @Test
    fun `ライトテーマ`() = captureDialog(darkTheme = false)

    @Test
    fun `ダークテーマ`() = captureDialog(darkTheme = true)

    private fun captureDialog(darkTheme: Boolean) =
        composeScreenshotTest {
            setContent {
                KoDriverTheme(darkTheme = darkTheme) {
                    Surface {
                        Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                            KoDriverDestructiveConfirmDialog(
                                title = "ログを削除",
                                body = "選択したログを削除します。この操作は取り消せません。",
                                confirmLabel = "削除",
                                dismissLabel = "キャンセル",
                                onConfirm = {},
                                onDismiss = {},
                            )
                        }
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }
}
