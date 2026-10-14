@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherconsoleipdetail

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OtherConsoleIpDetailUriHandlerTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val viewModel: OtherConsoleIpDetailViewModel = mockk(relaxUnitFun = true)

    @Test
    fun `接続設定ガイドリンクをクリックすると接続設定ガイドのURLが開かれる`() {
        every { viewModel.uiState } returns MutableStateFlow(OtherConsoleIpDetailUiState())
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    OtherConsoleIpDetailPane(canNavigateBack = false, onBack = {})
                }
            }
            rule.onNodeWithText("接続設定ガイドを開く").performScrollTo().performClick()

            val shadowActivity = shadowOf(rule.activity)
            val startedIntent = shadowActivity.nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, startedIntent.action)
            assertEquals(
                "https://github.com/ai-kurou/KoDriver/blob/main/docs/gt7-ps5-connection-setup.md",
                startedIntent.data.toString(),
            )
            assertNull(shadowActivity.nextStartedActivity)
            verify(exactly = 1) { viewModel.uiState }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `SimHubリンクをクリックするとSimHubのURLが開かれる`() {
        every { viewModel.uiState } returns MutableStateFlow(OtherConsoleIpDetailUiState())
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    OtherConsoleIpDetailPane(canNavigateBack = false, onBack = {})
                }
            }
            val node = rule.onNodeWithText("ゲーム機または", substring = true)
            val textLayoutResults = mutableListOf<TextLayoutResult>()
            node
                .fetchSemanticsNode()
                .config[SemanticsActions.GetTextLayoutResult]
                .action
                ?.invoke(textLayoutResults)
            val textLayoutResult = textLayoutResults.first()
            val linkStart =
                textLayoutResult.layoutInput.text.text
                    .indexOf("SimHub")
            val boundingBox = textLayoutResult.getBoundingBox(linkStart)
            node.performTouchInput { click(boundingBox.center) }

            val shadowActivity = shadowOf(rule.activity)
            val startedIntent = shadowActivity.nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, startedIntent.action)
            assertEquals("https://www.simhubdash.com/", startedIntent.data.toString())
            assertNull(shadowActivity.nextStartedActivity)
            verify(exactly = 1) { viewModel.uiState }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }
}
