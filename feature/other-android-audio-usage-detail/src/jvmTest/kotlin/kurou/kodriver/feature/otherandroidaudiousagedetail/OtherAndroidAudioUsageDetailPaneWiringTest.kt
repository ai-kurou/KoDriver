package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsSelected
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
import kurou.kodriver.domain.model.AndroidAudioUsage
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class OtherAndroidAudioUsageDetailPaneWiringTest {
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

    private val viewModel: OtherAndroidAudioUsageDetailViewModel = mockk()

    private val onBack: () -> Unit = mockk()

    @Test
    fun `PaneはKoinの用途を表示し選択と戻る操作を通知する`() {
        every { viewModel.uiState } returns
            MutableStateFlow(OtherAndroidAudioUsageDetailUiState(usage = AndroidAudioUsage.ALARM))
        every { viewModel.onUsageSelected(AndroidAudioUsage.MEDIA) } returns Unit
        every { viewModel.onPreviewStopped() } returns Unit
        every { viewModel.onPreviewClicked(AndroidAudioUsage.ALARM, "これは音声の用途の試聴です。") } returns Unit
        every { onBack() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    OtherAndroidAudioUsageDetailPane(true, onBack, Modifier.testTag("audio-usage-pane"))
                }
            }
            rule.onNodeWithTag("audio-usage-pane").assertExists()
            rule.onNodeWithText("アラーム").performScrollTo().assertIsSelected()
            rule.onNodeWithText("メディア").performScrollTo().performClick()
            rule.onNodeWithContentDescription("アラームを試聴").performScrollTo().performClick()
            rule.onNodeWithContentDescription("戻る").performClick()
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.onUsageSelected(AndroidAudioUsage.MEDIA) }
            verify(exactly = 1) { viewModel.onPreviewClicked(AndroidAudioUsage.ALARM, "これは音声の用途の試聴です。") }
            verify(exactly = 1) { onBack() }
            confirmVerified(viewModel, onBack)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `Paneの破棄で試聴を停止する`() {
        every { viewModel.uiState } returns MutableStateFlow(OtherAndroidAudioUsageDetailUiState())
        every { viewModel.onPreviewStopped() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            var visible by mutableStateOf(true)
            rule.setContent {
                KoDriverTheme {
                    if (visible) OtherAndroidAudioUsageDetailPane(true, {}, Modifier)
                }
            }
            rule.waitForIdle()
            visible = false
            rule.waitForIdle()
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.onPreviewStopped() }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }
}
