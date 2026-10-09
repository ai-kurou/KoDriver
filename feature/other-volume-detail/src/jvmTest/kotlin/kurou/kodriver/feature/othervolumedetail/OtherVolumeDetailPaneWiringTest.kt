package kurou.kodriver.feature.othervolumedetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVolumeDetailPaneWiringTest {
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

    private val viewModel: OtherVolumeDetailViewModel = mockk()

    private val onBack: () -> Unit = mockk()

    @Test
    fun `Paneは音量と試聴状態を表示し操作と離脱時の停止を通知する`() {
        val state = MutableStateFlow(OtherVolumeDetailUiState(volume = 80, deviceVolume = 60))
        // DisposableEffectのキー比較はUI操作の検証対象から除外する。
        excludeRecords { viewModel.equals(viewModel) }
        every { viewModel.uiState } returns state
        every { viewModel.onVolumeChanged(50) } returns Unit
        every { viewModel.onDeviceVolumeChanged(30) } returns Unit
        every { viewModel.onPreviewClicked() } returns Unit
        every { viewModel.onPreviewStopped() } returns Unit
        every { onBack() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            var showPane by mutableStateOf(true)
            rule.setContent {
                KoDriverTheme {
                    if (showPane) OtherVolumeDetailPane(true, onBack, Modifier.testTag("volume-pane"))
                }
            }
            rule.onNodeWithTag("volume-pane").assertExists()
            rule.onNodeWithText("80%").assertExists()
            rule.onNodeWithText("60%").assertExists()
            val slider =
                SemanticsMatcher("ProgressBarRangeInfoを持つスライダー") {
                    it.config.contains(SemanticsProperties.ProgressBarRangeInfo)
                }
            rule.onAllNodes(slider)[0].performSemanticsAction(SemanticsActions.SetProgress) { it(50f) }
            rule.onAllNodes(slider)[1].performSemanticsAction(SemanticsActions.SetProgress) { it(30f) }
            rule.onNodeWithText("試聴").performClick()
            state.update { it.copy(isPreviewing = true) }
            rule.onNodeWithText("停止").performClick()
            rule.onNodeWithContentDescription("戻る").performClick()
            showPane = false
            rule.waitForIdle()
            verify(exactly = 2) { viewModel.uiState }
            verify(exactly = 1) { viewModel.onPreviewStopped() }
            verify(exactly = 1) { viewModel.onVolumeChanged(50) }
            verify(exactly = 1) { viewModel.onDeviceVolumeChanged(30) }
            verify(exactly = 2) { viewModel.onPreviewClicked() }
            verify(exactly = 1) { onBack() }
            confirmVerified(viewModel, onBack)
        } finally {
            stopKoin()
        }
    }
}
