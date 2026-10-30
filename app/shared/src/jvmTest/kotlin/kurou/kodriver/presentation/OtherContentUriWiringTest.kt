package kurou.kodriver.presentation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
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
class OtherContentUriWiringTest {
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
    private val uriHandler: UriHandler = mockk()

    @Test
    fun `ログ削除の表示と確認とキャンセルをViewModelへ委譲する`() {
        val state = MutableStateFlow(OtherListUiState(items = listOf(OtherListItemType.DeleteAllLogs)))
        every { viewModel.uiState } returns state
        every { viewModel.checkUpdate() } returns Unit
        every { viewModel.checkStartupEnabled() } returns Unit
        every { viewModel.checkAccessLocalNetworkPermission() } returns Unit
        every { viewModel.consumeResetResult() } returns Unit
        every { viewModel.consumeAutoReadoutSettingsResetResult() } returns Unit
        every { viewModel.consumeManualReadoutSettingsResetResult() } returns Unit
        every { viewModel.onResetClick() } answers {
            state.update { it.copy(reset = it.reset.copy(showResetConfirmDialog = true)) }
        }
        every { viewModel.onResetDismiss() } answers {
            state.update { it.copy(reset = it.reset.copy(showResetConfirmDialog = false)) }
        }
        every { viewModel.onResetConfirm() } answers {
            state.update { it.copy(reset = it.reset.copy(showResetConfirmDialog = false)) }
        }
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent { AppTheme { OtherContent() } }
            rule.onNodeWithText("ログを全て削除").performClick()
            rule.onNodeWithText("キャンセル").performClick()
            rule.onNodeWithText("ログを全て削除").performClick()
            rule.onNodeWithText("削除").performClick()

            verify(exactly = 2) { viewModel.onResetClick() }
            verify(exactly = 1) { viewModel.onResetDismiss() }
            verify(exactly = 1) { viewModel.onResetConfirm() }
            // 初回表示と、ダイアログ表示状態の変化(表示・閉じる・再表示・確認)のたびに再購読される。
            verify(exactly = 4) { viewModel.uiState }
            verify(exactly = 1) { viewModel.checkUpdate() }
            verify(exactly = 1) { viewModel.checkStartupEnabled() }
            verify(exactly = 1) { viewModel.checkAccessLocalNetworkPermission() }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `自動設定初期化の表示と確認とキャンセルをViewModelへ委譲する`() {
        val state = MutableStateFlow(OtherListUiState(items = listOf(OtherListItemType.ResetAutoReadoutSettings)))
        every { viewModel.uiState } returns state
        every { viewModel.checkUpdate() } returns Unit
        every { viewModel.checkStartupEnabled() } returns Unit
        every { viewModel.checkAccessLocalNetworkPermission() } returns Unit
        every { viewModel.consumeAutoReadoutSettingsResetResult() } returns Unit
        every { viewModel.consumeManualReadoutSettingsResetResult() } returns Unit
        every { viewModel.consumeResetResult() } returns Unit
        every { viewModel.onAutoReadoutSettingsResetClick() } answers {
            state.update {
                it.copy(autoReadoutSettingsReset = it.autoReadoutSettingsReset.copy(showResetConfirmDialog = true))
            }
        }
        every { viewModel.onAutoReadoutSettingsResetDismiss() } answers {
            state.update {
                it.copy(autoReadoutSettingsReset = it.autoReadoutSettingsReset.copy(showResetConfirmDialog = false))
            }
        }
        every { viewModel.onAutoReadoutSettingsResetConfirm() } answers {
            state.update {
                it.copy(autoReadoutSettingsReset = it.autoReadoutSettingsReset.copy(showResetConfirmDialog = false))
            }
        }
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent { AppTheme { OtherContent() } }
            rule.onNodeWithText("自動タブの設定を初期化").performClick()
            rule.onNodeWithText("キャンセル").performClick()
            rule.onNodeWithText("自動タブの設定を初期化").performClick()
            rule.onNodeWithText("初期化").performClick()

            verify(exactly = 2) { viewModel.onAutoReadoutSettingsResetClick() }
            verify(exactly = 1) { viewModel.onAutoReadoutSettingsResetDismiss() }
            verify(exactly = 1) { viewModel.onAutoReadoutSettingsResetConfirm() }
            // 初回表示と、ダイアログ表示状態の変化(表示・閉じる・再表示・確認)のたびに再購読される。
            verify(exactly = 4) { viewModel.uiState }
            verify(exactly = 1) { viewModel.checkUpdate() }
            verify(exactly = 1) { viewModel.checkStartupEnabled() }
            verify(exactly = 1) { viewModel.checkAccessLocalNetworkPermission() }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `手動設定初期化の表示と確認とキャンセルをViewModelへ委譲する`() {
        val state = MutableStateFlow(OtherListUiState(items = listOf(OtherListItemType.ResetManualReadoutSettings)))
        every { viewModel.uiState } returns state
        every { viewModel.checkUpdate() } returns Unit
        every { viewModel.checkStartupEnabled() } returns Unit
        every { viewModel.checkAccessLocalNetworkPermission() } returns Unit
        every { viewModel.consumeManualReadoutSettingsResetResult() } returns Unit
        every { viewModel.consumeAutoReadoutSettingsResetResult() } returns Unit
        every { viewModel.consumeResetResult() } returns Unit
        every { viewModel.onManualReadoutSettingsResetClick() } answers {
            state.update {
                it.copy(manualReadoutSettingsReset = it.manualReadoutSettingsReset.copy(showResetConfirmDialog = true))
            }
        }
        every { viewModel.onManualReadoutSettingsResetDismiss() } answers {
            state.update {
                it.copy(manualReadoutSettingsReset = it.manualReadoutSettingsReset.copy(showResetConfirmDialog = false))
            }
        }
        every { viewModel.onManualReadoutSettingsResetConfirm() } answers {
            state.update {
                it.copy(manualReadoutSettingsReset = it.manualReadoutSettingsReset.copy(showResetConfirmDialog = false))
            }
        }
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent { AppTheme { OtherContent() } }
            rule.onNodeWithText("手動タブの設定を初期化").performClick()
            rule.onNodeWithText("キャンセル").performClick()
            rule.onNodeWithText("手動タブの設定を初期化").performClick()
            rule.onNodeWithText("初期化").performClick()

            verify(exactly = 2) { viewModel.onManualReadoutSettingsResetClick() }
            verify(exactly = 1) { viewModel.onManualReadoutSettingsResetDismiss() }
            verify(exactly = 1) { viewModel.onManualReadoutSettingsResetConfirm() }
            // 初回表示と、ダイアログ表示状態の変化(表示・閉じる・再表示・確認)のたびに再購読される。
            verify(exactly = 4) { viewModel.uiState }
            verify(exactly = 1) { viewModel.checkUpdate() }
            verify(exactly = 1) { viewModel.checkStartupEnabled() }
            verify(exactly = 1) { viewModel.checkAccessLocalNetworkPermission() }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `GitHubレポジトリリンクをクリックするとGitHubレポジトリのURLが開かれる`() {
        every { viewModel.uiState } returns
            MutableStateFlow(OtherListUiState(items = listOf(OtherListItemType.GitHubRepository)))
        every { viewModel.checkUpdate() } returns Unit
        every { viewModel.checkStartupEnabled() } returns Unit
        every { viewModel.checkAccessLocalNetworkPermission() } returns Unit
        every { viewModel.consumeResetResult() } returns Unit
        every { viewModel.consumeAutoReadoutSettingsResetResult() } returns Unit
        every { viewModel.consumeManualReadoutSettingsResetResult() } returns Unit
        every { uriHandler.openUri("https://github.com/ai-kurou/KoDriver") } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                    AppTheme {
                        OtherContent()
                    }
                }
            }
            rule.onNodeWithText("GitHubレポジトリ").performClick()

            verify(exactly = 1) { uriHandler.openUri("https://github.com/ai-kurou/KoDriver") }
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.checkUpdate() }
            verify(exactly = 1) { viewModel.checkStartupEnabled() }
            verify(exactly = 1) { viewModel.checkAccessLocalNetworkPermission() }
            confirmVerified(viewModel, uriHandler)
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
        every { viewModel.consumeResetResult() } returns Unit
        every { viewModel.consumeAutoReadoutSettingsResetResult() } returns Unit
        every { viewModel.consumeManualReadoutSettingsResetResult() } returns Unit
        every { uriHandler.openUri("https://github.com/ai-kurou/KoDriver/releases") } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                    AppTheme {
                        OtherContent()
                    }
                }
            }
            rule.onNodeWithText("リリースページ").performClick()

            verify(exactly = 1) { uriHandler.openUri("https://github.com/ai-kurou/KoDriver/releases") }
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.checkUpdate() }
            verify(exactly = 1) { viewModel.checkStartupEnabled() }
            verify(exactly = 1) { viewModel.checkAccessLocalNetworkPermission() }
            confirmVerified(viewModel, uriHandler)
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
        every { viewModel.consumeResetResult() } returns Unit
        every { viewModel.consumeAutoReadoutSettingsResetResult() } returns Unit
        every { viewModel.consumeManualReadoutSettingsResetResult() } returns Unit
        every {
            uriHandler.openUri(
                "https://play.google.com/store/apps/details?id=com.google.android.tts",
            )
        } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                    AppTheme {
                        OtherContent()
                    }
                }
            }
            rule.onNodeWithText("音声合成アプリをインストール").performClick()

            verify(exactly = 1) {
                uriHandler.openUri(
                    "https://play.google.com/store/apps/details?id=com.google.android.tts",
                )
            }
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.checkUpdate() }
            verify(exactly = 1) { viewModel.checkStartupEnabled() }
            verify(exactly = 1) { viewModel.checkAccessLocalNetworkPermission() }
            confirmVerified(viewModel, uriHandler)
        } finally {
            stopKoin()
        }
    }
}
