package kurou.kodriver.feature.othervoicespeeddetail

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
class OtherVoiceSpeedDetailPaneWiringTest {
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

    private val viewModel: OtherVoiceSpeedDetailViewModel = mockk()

    private val onBack: () -> Unit = mockk()

    @Test
    fun `PaneはKoinの速度を表示し変更とリセットと戻る操作を通知する`() {
        every { viewModel.uiState } returns MutableStateFlow(OtherVoiceSpeedDetailUiState(speed = 1.5f))
        every { viewModel.onSpeedChanged(2.0f) } returns Unit
        every { viewModel.onSpeedChanged(1.0f) } returns Unit
        every { onBack() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme { OtherVoiceSpeedDetailPane(true, onBack, Modifier.testTag("voice-speed-pane")) }
            }
            rule.onNodeWithTag("voice-speed-pane").assertExists()
            rule.onNodeWithText("1.5倍").assertExists()
            rule
                .onNode(
                    SemanticsMatcher("ProgressBarRangeInfoを持つスライダー") {
                        it.config.contains(SemanticsProperties.ProgressBarRangeInfo)
                    },
                ).performSemanticsAction(SemanticsActions.SetProgress) { it(2.0f) }
            rule.onNodeWithContentDescription("読み上げ速度をデフォルトに戻す").performClick()
            rule.onNodeWithContentDescription("戻る").performClick()
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.onSpeedChanged(2.0f) }
            verify(exactly = 1) { viewModel.onSpeedChanged(1.0f) }
            verify(exactly = 1) { onBack() }
            confirmVerified(viewModel, onBack)
        } finally {
            stopKoin()
        }
    }
}
