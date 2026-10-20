@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OtherListPaneAndroidAudioUsageTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `音声の用途を表示し詳細画面がない間は選択できない`() {
        rule.setContent {
            KoDriverTheme {
                OtherListPane(
                    uiState = OtherListUiState(items = listOf(OtherListItemType.AndroidAudioUsage)),
                    onItemClick = {},
                    onOverlayVisibleChange = {},
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                    onHapticFeedbackEnabledChange = {},
                    onStartupEnabledChange = {},
                )
            }
        }

        rule.onNodeWithText("音声の用途").assertIsDisplayed().assertIsNotEnabled()
    }
}
