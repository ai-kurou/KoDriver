@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListUiState
import kurou.kodriver.feature.otherlist.OtherListViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3AdaptiveApi::class)
class OtherContentSpeechRecognitionTest {
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

    private val viewModel: OtherListViewModel = mockk()

    private fun stubViewModel() {
        every { viewModel.uiState } returns
            MutableStateFlow(OtherListUiState(items = listOf(OtherListItemType.Voice)))
        every { viewModel.checkUpdate() } returns Unit
        every { viewModel.checkStartupEnabled() } returns Unit
        every { viewModel.checkAccessLocalNetworkPermission() } returns Unit
        every { viewModel.consumeResetResult() } returns Unit
        every { viewModel.consumeAutoReadoutSettingsResetResult() } returns Unit
        every { viewModel.consumeManualReadoutSettingsResetResult() } returns Unit
    }

    @Test
    fun `渡された音声認識のカードを一覧に表示する`() {
        rule.setContent {
            AppTheme {
                OtherContent(
                    uiState = OtherListUiState(items = listOf(OtherListItemType.Voice)),
                    onItemSelected = {},
                    onClearSelectedItem = {},
                    speechRecognitionSection = { Text("音声認識スロット") },
                )
            }
        }

        rule.onNodeWithText("音声認識スロット").assertIsDisplayed()
    }

    @Test
    fun `CompositionLocalで提供された音声認識のカードを一覧に表示する`() {
        stubViewModel()
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                AppTheme {
                    CompositionLocalProvider(LocalSpeechRecognitionSection provides { Text("提供されたスロット") }) {
                        OtherContent()
                    }
                }
            }

            rule.onNodeWithText("提供されたスロット").assertIsDisplayed()
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `音声認識のカードが提供されない場合は何も表示しない`() {
        stubViewModel()
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent { AppTheme { OtherContent() } }

            rule.onNodeWithText("提供されたスロット").assertDoesNotExist()
            rule.onNodeWithText("読み上げ音声").assertIsDisplayed()
        } finally {
            stopKoin()
        }
    }
}
