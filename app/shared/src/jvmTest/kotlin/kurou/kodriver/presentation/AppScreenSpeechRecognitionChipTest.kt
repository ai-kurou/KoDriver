package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kurou.kodriver.feature.main.SPEECH_RECOGNITION_CHIP_OVERLAY_INSET
import kurou.kodriver.feature.main.SpeechRecognitionDetailPresentation
import kurou.kodriver.feature.main.speechRecognitionChipOverlayInset
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun `各タブの画面へチップが占める幅と高さを渡す`() {
        val insets = mutableListOf<Dp>()
        rule.setContent {
            CompositionLocalProvider(LocalSpeechRecognitionChip provides fakeChip) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    autoReadoutContent = { insets += speechRecognitionChipOverlayInset.compositionLocal.current },
                )
            }
        }

        rule.runOnIdle { assertEquals(setOf(SPEECH_RECOGNITION_CHIP_OVERLAY_INSET), insets.toSet()) }
    }

    @Test
    fun `Snackbarはチップに重ならないよう右端を空けて表示する`() {
        val snackbarHostState = SnackbarHostState()
        rule.setContent {
            CompositionLocalProvider(LocalSpeechRecognitionChip provides fakeChip) {
                Box(Modifier.size(width = 400.dp, height = 800.dp)) {
                    AppScreenContent(
                        layoutType = NavigationSuiteType.NavigationBar,
                        snackbarHostState = snackbarHostState,
                    )
                }
            }
        }
        rule.runOnIdle {
            CoroutineScope(Dispatchers.Unconfined).launch { snackbarHostState.showSnackbar(LONG_MESSAGE) }
        }

        rule.waitUntil { rule.onAllNodesWithText(LONG_MESSAGE).fetchSemanticsNodes().isNotEmpty() }
        val snackbarNode = rule.onNodeWithText(LONG_MESSAGE).fetchSemanticsNode()
        val snackbarRight = snackbarNode.boundsInRoot.right
        val chipNode = rule.onNodeWithTag("chip").fetchSemanticsNode()
        val chipLeft = chipNode.boundsInRoot.left
        assertTrue(snackbarRight <= chipLeft, "Snackbarの右端($snackbarRight)がチップの左端($chipLeft)を越えている")
    }

    private companion object {
        val LONG_MESSAGE = "とても長いメッセージ".repeat(20)
    }
}
