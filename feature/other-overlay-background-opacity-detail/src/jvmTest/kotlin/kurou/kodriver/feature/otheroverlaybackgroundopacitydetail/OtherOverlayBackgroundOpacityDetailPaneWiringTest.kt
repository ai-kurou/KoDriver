package kurou.kodriver.feature.otheroverlaybackgroundopacitydetail

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
class OtherOverlayBackgroundOpacityDetailPaneWiringTest {
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

    private val viewModel: OtherOverlayBackgroundOpacityDetailViewModel = mockk()

    private val onBack: () -> Unit = mockk()

    @Test
    fun `PaneはKoinの透明度を表示しプレビューと保存と戻る操作と離脱時の解除を通知する`() {
        // DisposableEffectのキー比較はUI操作の検証対象から除外する。
        excludeRecords { viewModel.equals(viewModel) }
        every { viewModel.uiState } returns MutableStateFlow(OtherOverlayBackgroundOpacityDetailUiState(opacity = 50))
        every { viewModel.onOpacityPreview(80) } returns Unit
        every { viewModel.onOpacityChanged(80) } returns Unit
        every { viewModel.onPreviewStopped() } returns Unit
        every { onBack() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            var showPane by mutableStateOf(true)
            rule.setContent {
                KoDriverTheme {
                    if (showPane) {
                        OtherOverlayBackgroundOpacityDetailPane(
                            canNavigateBack = true,
                            onBack = onBack,
                            modifier = Modifier.testTag("overlay-background-opacity-pane"),
                        )
                    }
                }
            }
            rule.onNodeWithTag("overlay-background-opacity-pane").assertExists()
            rule
                .onNode(
                    SemanticsMatcher("ProgressBarRangeInfoを持つスライダー") {
                        it.config.contains(SemanticsProperties.ProgressBarRangeInfo)
                    },
                ).performSemanticsAction(SemanticsActions.SetProgress) { it(80f) }
            rule.onNodeWithContentDescription("戻る").performClick()
            showPane = false
            rule.waitForIdle()

            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.onOpacityPreview(80) }
            verify(exactly = 1) { viewModel.onOpacityChanged(80) }
            verify(exactly = 1) { viewModel.onPreviewStopped() }
            verify(exactly = 1) { onBack() }
            confirmVerified(viewModel, onBack)
        } finally {
            stopKoin()
        }
    }
}
