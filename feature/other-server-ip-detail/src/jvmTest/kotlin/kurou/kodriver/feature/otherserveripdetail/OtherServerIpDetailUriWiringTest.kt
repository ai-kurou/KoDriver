package kurou.kodriver.feature.otherserveripdetail

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
class OtherServerIpDetailUriWiringTest {
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

    private val viewModel: OtherServerIpDetailViewModel = mockk()
    private val uriHandler: UriHandler = mockk()

    @Test
    fun `インストールガイドリンクをクリックするとインストールガイドのURLが開かれる`() {
        every { viewModel.uiState } returns MutableStateFlow(OtherServerIpDetailUiState())
        every {
            uriHandler.openUri(
                "https://github.com/ai-kurou/KoDriver/blob/main/docs/windows-install.md",
            )
        } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                    KoDriverTheme {
                        OtherServerIpDetailPane(canNavigateBack = false, onBack = {})
                    }
                }
            }
            rule.onNodeWithText("インストールガイドを開く").performClick()

            verify(exactly = 1) {
                uriHandler.openUri(
                    "https://github.com/ai-kurou/KoDriver/blob/main/docs/windows-install.md",
                )
            }
            verify(exactly = 1) { viewModel.uiState }
            confirmVerified(viewModel, uriHandler)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `説明文のWindows版KoDriverリンクをクリックするとインストールガイドのURLが開かれる`() {
        every { viewModel.uiState } returns MutableStateFlow(OtherServerIpDetailUiState())
        every { uriHandler.openUri("https://github.com/ai-kurou/KoDriver/blob/main/docs/windows-install.md") } returns
            Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                    KoDriverTheme {
                        OtherServerIpDetailPane(canNavigateBack = false, onBack = {})
                    }
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

            verify(
                exactly = 1,
            ) { uriHandler.openUri("https://github.com/ai-kurou/KoDriver/blob/main/docs/windows-install.md") }
            verify(exactly = 1) { viewModel.uiState }
            confirmVerified(viewModel, uriHandler)
        } finally {
            stopKoin()
        }
    }
}
