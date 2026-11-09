package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kurou.kodriver.feature.main.SpeechRecognitionDetailPresentation
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AppScreenSpeechRecognitionChipTest {
    @get:Rule
    val rule = createComposeRule()

    private val presentations = mutableListOf<SpeechRecognitionDetailPresentation>()

    private val fakeChip: @Composable (Modifier, SpeechRecognitionDetailPresentation) -> Unit =
        { modifier, presentation ->
            presentations += presentation
            Text("chip", modifier = modifier.testTag("chip"))
        }

    @Test
    fun `チップが提供されているとき全画面に重ねて表示する`() {
        rule.setContent {
            CompositionLocalProvider(
                LocalSpeechRecognitionChip provides fakeChip,
            ) {
                AppScreenContent(layoutType = NavigationSuiteType.NavigationBar)
            }
        }

        rule.onNodeWithTag("chip").assertIsDisplayed()
        rule.runOnIdle { assertEquals(setOf(SpeechRecognitionDetailPresentation.BottomSheet), presentations.toSet()) }
    }

    @Test
    fun `広い画面でもチップを表示する`() {
        rule.setContent {
            CompositionLocalProvider(
                LocalSpeechRecognitionChip provides fakeChip,
            ) {
                AppScreenContent(layoutType = NavigationSuiteType.NavigationRail)
            }
        }

        rule.onNodeWithTag("chip").assertIsDisplayed()
        rule.runOnIdle { assertEquals(setOf(SpeechRecognitionDetailPresentation.Popover), presentations.toSet()) }
    }

    @Test
    fun `チップが提供されていないときは何も表示しない`() {
        rule.setContent { AppScreenContent(layoutType = NavigationSuiteType.NavigationBar) }

        rule.onNodeWithTag("chip").assertDoesNotExist()
    }
}
