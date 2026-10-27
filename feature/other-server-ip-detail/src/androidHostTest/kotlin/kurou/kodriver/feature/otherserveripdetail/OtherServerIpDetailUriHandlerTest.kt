@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherserveripdetail

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class OtherServerIpDetailUriHandlerTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val viewModel: OtherServerIpDetailViewModel = mockk(relaxUnitFun = true)

    @Test
    fun `インストールガイドリンクをクリックするとインストールガイドのURLが開かれる`() {
        every { viewModel.uiState } returns MutableStateFlow(OtherServerIpDetailUiState())
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    OtherServerIpDetailPane(canNavigateBack = false, onBack = {})
                }
            }
            rule.onNodeWithText("インストールガイドを開く").performClick()

            val shadowActivity = shadowOf(rule.activity)
            val startedIntent = shadowActivity.nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, startedIntent.action)
            assertEquals(
                "https://github.com/ai-kurou/KoDriver/blob/main/docs/windows-install.md",
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
    fun `説明文のWindows版KoDriverリンクをクリックするとインストールガイドのURLが開かれる`() {
        every { viewModel.uiState } returns MutableStateFlow(OtherServerIpDetailUiState())
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    OtherServerIpDetailPane(canNavigateBack = false, onBack = {})
                }
            }
            val node = rule.onNodeWithText("が動作しているPC", substring = true)
            val textLayoutResults = mutableListOf<TextLayoutResult>()
            node
                .fetchSemanticsNode()
                .config[SemanticsActions.GetTextLayoutResult]
                .action
                ?.invoke(textLayoutResults)
            val textLayoutResult = textLayoutResults.first()
            val linkStart =
                textLayoutResult.layoutInput.text.text
                    .indexOf("Windows版KoDriver")
            val boundingBox = textLayoutResult.getBoundingBox(linkStart)
            node.performTouchInput { click(boundingBox.center) }

            val shadowActivity = shadowOf(rule.activity)
            val startedIntent = shadowActivity.nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, startedIntent.action)
            assertEquals(
                "https://github.com/ai-kurou/KoDriver/blob/main/docs/windows-install.md",
                startedIntent.data.toString(),
            )
            assertNull(shadowActivity.nextStartedActivity)
            verify(exactly = 1) { viewModel.uiState }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }
}
