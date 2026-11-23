package kurou.kodriver.feature.splash

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class SplashBackdropTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `光の進捗が範囲内でも範囲外でも内容が表示される`() {
        var progress by mutableFloatStateOf(0f)

        rule.setContent {
            MaterialTheme {
                SplashBackdrop(shineProgress = progress) {
                    Text("内容")
                }
            }
        }

        listOf(0f, 0.5f, 1f).forEach {
            progress = it
            rule.waitForIdle()

            rule.onNodeWithText("内容").assertIsDisplayed()
        }
    }

    @Test
    fun `アプリアイコンが表示される`() {
        rule.setContent {
            MaterialTheme {
                SplashAppIcon()
            }
        }

        rule.onNodeWithContentDescription("KoDriver").assertIsDisplayed()
    }
}
