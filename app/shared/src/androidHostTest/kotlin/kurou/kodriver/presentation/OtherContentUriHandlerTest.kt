@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListUiState
import kurou.kodriver.feature.otherlist.OtherListViewModel
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OtherContentUriHandlerTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val viewModel: OtherListViewModel = mockk(relaxUnitFun = true)

    @Test
    fun `GitHubレポジトリリンクをクリックするとGitHubレポジトリのURLが開かれる`() {
        every { viewModel.uiState } returns
            MutableStateFlow(OtherListUiState(items = listOf(OtherListItemType.GitHubRepository)))
        every { viewModel.checkUpdate() } returns Unit
        every { viewModel.checkStartupEnabled() } returns Unit
        every { viewModel.checkAccessLocalNetworkPermission() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                AppTheme {
                    OtherContent()
                }
            }
            rule.onNodeWithText("GitHubレポジトリ").performClick()

            val shadowActivity = shadowOf(rule.activity)
            val startedIntent = shadowActivity.nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, startedIntent.action)
            assertEquals("https://github.com/ai-kurou/KoDriver", startedIntent.data.toString())
            assertNull(shadowActivity.nextStartedActivity)
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.checkUpdate() }
            verify(exactly = 1) { viewModel.checkStartupEnabled() }
            verify(exactly = 1) { viewModel.checkAccessLocalNetworkPermission() }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `リリースページリンクをクリックするとリリースページのURLが開かれる`() {
        every { viewModel.uiState } returns
            MutableStateFlow(OtherListUiState(items = listOf(OtherListItemType.ReleasePage)))
        every { viewModel.checkUpdate() } returns Unit
        every { viewModel.checkStartupEnabled() } returns Unit
        every { viewModel.checkAccessLocalNetworkPermission() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                AppTheme {
                    OtherContent()
                }
            }
            rule.onNodeWithText("リリースページ").performClick()

            val shadowActivity = shadowOf(rule.activity)
            val startedIntent = shadowActivity.nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, startedIntent.action)
            assertEquals("https://github.com/ai-kurou/KoDriver/releases", startedIntent.data.toString())
            assertNull(shadowActivity.nextStartedActivity)
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.checkUpdate() }
            verify(exactly = 1) { viewModel.checkStartupEnabled() }
            verify(exactly = 1) { viewModel.checkAccessLocalNetworkPermission() }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `音声合成アプリリンクをクリックすると音声合成アプリのURLが開かれる`() {
        every { viewModel.uiState } returns
            MutableStateFlow(OtherListUiState(items = listOf(OtherListItemType.TtsEngineMissing)))
        every { viewModel.checkUpdate() } returns Unit
        every { viewModel.checkStartupEnabled() } returns Unit
        every { viewModel.checkAccessLocalNetworkPermission() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                AppTheme {
                    OtherContent()
                }
            }
            rule.onNodeWithText("音声合成アプリをインストール").performClick()

            val shadowActivity = shadowOf(rule.activity)
            val startedIntent = shadowActivity.nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, startedIntent.action)
            assertEquals(
                "https://play.google.com/store/apps/details?id=com.google.android.tts",
                startedIntent.data.toString(),
            )
            assertNull(shadowActivity.nextStartedActivity)
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.checkUpdate() }
            verify(exactly = 1) { viewModel.checkStartupEnabled() }
            verify(exactly = 1) { viewModel.checkAccessLocalNetworkPermission() }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }
}
