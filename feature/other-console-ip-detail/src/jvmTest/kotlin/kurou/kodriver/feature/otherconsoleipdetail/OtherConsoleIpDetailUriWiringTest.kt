package kurou.kodriver.feature.otherconsoleipdetail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
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
class OtherConsoleIpDetailUriWiringTest {
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

    private val viewModel: OtherConsoleIpDetailViewModel = mockk()
    private val uriHandler: UriHandler = mockk()

    @Test
    fun `接続設定ガイドリンクをクリックすると接続設定ガイドのURLが開かれる`() {
        every { viewModel.uiState } returns MutableStateFlow(OtherConsoleIpDetailUiState())
        every {
            uriHandler.openUri(
                "https://github.com/ai-kurou/KoDriver/blob/main/docs/gt7-ps5-connection-setup.md",
            )
        } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                    KoDriverTheme {
                        OtherConsoleIpDetailPane(canNavigateBack = false, onBack = {})
                    }
                }
            }
            rule.onNodeWithText("接続設定ガイドを開く").performClick()

            verify(exactly = 1) {
                uriHandler.openUri(
                    "https://github.com/ai-kurou/KoDriver/blob/main/docs/gt7-ps5-connection-setup.md",
                )
            }
            verify(exactly = 1) { viewModel.uiState }
            confirmVerified(viewModel, uriHandler)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `SimHubリンクをクリックするとSimHubのURLが開かれる`() {
        every { viewModel.uiState } returns MutableStateFlow(OtherConsoleIpDetailUiState())
        every { uriHandler.openUri("https://www.simhubdash.com/") } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                    KoDriverTheme {
                        OtherConsoleIpDetailPane(canNavigateBack = false, onBack = {})
                    }
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

            verify(exactly = 1) { uriHandler.openUri("https://www.simhubdash.com/") }
            verify(exactly = 1) { viewModel.uiState }
            confirmVerified(viewModel, uriHandler)
        } finally {
            stopKoin()
        }
    }
}
