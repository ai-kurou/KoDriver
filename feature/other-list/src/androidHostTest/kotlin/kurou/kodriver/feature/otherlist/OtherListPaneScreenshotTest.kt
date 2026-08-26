@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.performScrollToNode
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
@Config(sdk = [36], qualifiers = "w360dp-h1080dp")
class OtherListPaneScreenshotTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OtherListPane(
                            uiState = OtherListUiState(),
                            onItemClick = {},
                            onKeepScreenOnChange = {},
                            onDynamicColorEnabledChange = {},
                            onHapticFeedbackEnabledChange = {},
                            onStartupEnabledChange = {},
                        )
                    }
                }
            }
        }

        rule.activity.window.decorView
            .captureRoboImage(roborazziOptions = defaultRoborazziOptions)
    }

    @Test
    fun `アップデートバッジを表示`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OtherListPane(
                            uiState = OtherListUiState(hasAppUpdate = true),
                            onItemClick = {},
                            onKeepScreenOnChange = {},
                            onDynamicColorEnabledChange = {},
                            onHapticFeedbackEnabledChange = {},
                            onStartupEnabledChange = {},
                        )
                    }
                }
            }
        }

        rule.onNode(hasScrollAction()).performScrollToNode(hasText("リリースページ"))
        rule.activity.window.decorView
            .captureRoboImage(roborazziOptions = defaultRoborazziOptions)
    }

    @Test
    fun `振動機能がない端末ではハプティックフィードバック項目が表示されない`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OtherListPane(
                            uiState =
                                OtherListUiState(
                                    items = buildOtherListItems().filterNot { it == OtherListItemType.HapticFeedback },
                                ),
                            onItemClick = {},
                            onKeepScreenOnChange = {},
                            onDynamicColorEnabledChange = {},
                            onHapticFeedbackEnabledChange = {},
                            onStartupEnabledChange = {},
                        )
                    }
                }
            }
        }

        rule.onNode(hasScrollAction()).performScrollToNode(hasText("ライセンス"))
        rule.activity.window.decorView
            .captureRoboImage(roborazziOptions = defaultRoborazziOptions)
    }
}
