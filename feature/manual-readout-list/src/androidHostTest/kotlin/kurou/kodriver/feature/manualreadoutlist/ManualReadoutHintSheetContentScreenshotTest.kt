@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.defaultRoborazziOptions
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w480dp-h800dp")
class ManualReadoutHintSheetContentScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `Android版ではホットキーの説明を表示しない`() {
        composeRule.setContent {
            KoDriverTheme {
                Surface {
                    ManualReadoutHintSheetContent(showHotkeyHint = isManualReadoutHotkeySupported())
                }
            }
        }
        composeRule
            .onNodeWithText("標準表示では、セットを長押しすると、確認してから削除できます。再生ボタン表示では、編集画面から削除します。")
            .assertIsDisplayed()
        composeRule.onRoot().captureRoboImage(roborazziOptions = defaultRoborazziOptions)
    }
}
