package kurou.kodriver.feature.othervoicedetail

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.TextToSpeechVoice
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVoiceDetailPaneWiringTest {
    private val rule = createComposeRule()

    @get:Rule
    val rules: TestRule =
        RuleChain
            .outerRule(
                object : ExternalResource() {
                    override fun before() {
                        Dispatchers.setMain(UnconfinedTestDispatcher())
                    }

                    override fun after() {
                        Dispatchers.resetMain()
                    }
                },
            ).around(rule)

    private val viewModel: OtherVoiceDetailViewModel = mockk()

    private val onBack: () -> Unit = mockk()

    @Test
    fun `PaneはKoinの状態を表示し音声選択と試聴と戻る操作を通知する`() {
        every { viewModel.uiState } returns
            MutableStateFlow(
                OtherVoiceDetailUiState(
                    voices = listOf(TextToSpeechVoice("voice-a", "音声A", "ja-JP")),
                    isLoading = false,
                ),
            )
        every { viewModel.onVoiceSelected("voice-a") } returns Unit
        every { viewModel.onPreviewClicked("voice-a", "これは読み上げ音声の試聴です。") } returns Unit
        every { onBack() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme { OtherVoiceDetailPane(true, onBack, Modifier.testTag("voice-pane")) }
            }
            rule.onNodeWithTag("voice-pane").assertExists()
            rule.onNodeWithText("音声A").performScrollTo().performClick()
            rule.onNodeWithContentDescription("音声Aを試聴").performScrollTo().performClick()
            rule.onNodeWithContentDescription("戻る").performClick()
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.onVoiceSelected("voice-a") }
            verify(exactly = 1) { viewModel.onPreviewClicked("voice-a", "これは読み上げ音声の試聴です。") }
            verify(exactly = 1) { onBack() }
            confirmVerified(viewModel, onBack)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `Paneは空の状態で再読み込みを通知し戻れない場合は戻る操作を隠す`() {
        every { viewModel.uiState } returns MutableStateFlow(OtherVoiceDetailUiState(isLoading = false))
        every { viewModel.onRetryClicked() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent { KoDriverTheme { OtherVoiceDetailPane(false, onBack) } }
            rule.onNodeWithContentDescription("戻る").assertDoesNotExist()
            rule.onNodeWithText("再読み込み").performScrollTo().performClick()
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.onRetryClicked() }
            verify(exactly = 0) { onBack() }
            confirmVerified(viewModel, onBack)
        } finally {
            stopKoin()
        }
    }
}
