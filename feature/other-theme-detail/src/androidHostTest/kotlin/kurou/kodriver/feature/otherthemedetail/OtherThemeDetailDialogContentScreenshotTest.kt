@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherthemedetail

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ThemeMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w480dp-h320dp")
class OtherThemeDetailDialogContentScreenshotTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `システムに従うを選択中`() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OtherThemeDetailDialogContent(
                            uiState = OtherThemeDetailUiState(pendingThemeMode = ThemeMode.SYSTEM),
                        )
                    }
                }
            }
        }

        rule.activity.window.decorView
            .captureRoboImage(roborazziOptions = defaultRoborazziOptions)
    }

    @Test
    fun `ダークを選択中`() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OtherThemeDetailDialogContent(
                            uiState = OtherThemeDetailUiState(pendingThemeMode = ThemeMode.DARK),
                        )
                    }
                }
            }
        }

        rule.activity.window.decorView
            .captureRoboImage(roborazziOptions = defaultRoborazziOptions)
    }
}
