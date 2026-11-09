package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class AppScreenSpeechRecognitionChipTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `チップが提供されているとき全画面に重ねて表示する`() {
        rule.setContent {
            CompositionLocalProvider(
                LocalSpeechRecognitionChip provides { modifier -> Text("chip", modifier = modifier.testTag("chip")) },
            ) {
                AppScreenContent(layoutType = NavigationSuiteType.NavigationBar)
            }
        }

        rule.onNodeWithTag("chip").assertIsDisplayed()
    }

    @Test
    fun `広い画面でもチップを表示する`() {
        rule.setContent {
            CompositionLocalProvider(
                LocalSpeechRecognitionChip provides { modifier -> Text("chip", modifier = modifier.testTag("chip")) },
            ) {
                AppScreenContent(layoutType = NavigationSuiteType.NavigationRail)
            }
        }

        rule.onNodeWithTag("chip").assertIsDisplayed()
    }

    @Test
    fun `チップが提供されていないときは何も表示しない`() {
        rule.setContent { AppScreenContent(layoutType = NavigationSuiteType.NavigationBar) }

        rule.onNodeWithTag("chip").assertDoesNotExist()
    }
}
