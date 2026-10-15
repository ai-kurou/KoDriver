@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherfeedbackdetail

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
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
class OtherFeedbackDetailUriHandlerTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val viewModel: OtherFeedbackDetailViewModel = mockk(relaxUnitFun = true)

    @Test
    fun `SentryリンクをクリックするとSentryのURLが開かれる`() {
        every { viewModel.uiState } returns MutableStateFlow(OtherFeedbackDetailUiState())
        every { viewModel.setTelemetryLogId(null) } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    OtherFeedbackDetailPane(canNavigateBack = false, onBack = {})
                }
            }
            // Robolectric にはフォントが無くテキスト幅が実機と一致しないため、getBoundingBox ではなくノード左端付近をタップする
            rule
                .onNodeWithText("Sentry", substring = true)
                .performScrollTo()
                .performTouchInput { click(Offset(0f, height / 2f)) }

            val shadowActivity = shadowOf(rule.activity)
            val startedIntent = shadowActivity.nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, startedIntent.action)
            assertEquals("https://sentry.io/", startedIntent.data.toString())
            assertNull(shadowActivity.nextStartedActivity)
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.setTelemetryLogId(null) }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }
}
