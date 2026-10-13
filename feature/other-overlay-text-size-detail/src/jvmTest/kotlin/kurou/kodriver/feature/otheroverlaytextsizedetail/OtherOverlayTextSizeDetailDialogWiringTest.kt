package kurou.kodriver.feature.otheroverlaytextsizedetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import kurou.kodriver.domain.model.OverlayTextSize
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class OtherOverlayTextSizeDetailDialogWiringTest {
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

    private val viewModel: OtherOverlayTextSizeDetailViewModel = mockk()
    private val onDismiss: () -> Unit = mockk()

    @Test
    fun `ダイアログは保存サイズを表示し選択とOKを通知する`() {
        // DisposableEffectのキー比較はUI操作の検証対象から除外する。
        excludeRecords { viewModel.equals(viewModel) }
        every { viewModel.onPreviewStopped() } returns Unit
        every { viewModel.uiState } returns
            MutableStateFlow(OtherOverlayTextSizeDetailUiState(pendingOverlayTextSize = OverlayTextSize.MEDIUM))
        every { viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.LARGE) } returns Unit
        every { viewModel.onConfirm() } returns Unit
        every { onDismiss() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            var showDialog by mutableStateOf(true)
            rule.setContent {
                if (showDialog) {
                    OtherOverlayTextSizeDetailDialog(onDismiss)
                }
            }
            rule.onNodeWithText("中").assertExists()
            rule.onNodeWithText("大").performScrollTo().performClick()
            rule.onNodeWithText("OK").performClick()
            showDialog = false
            rule.waitForIdle()
            verify(exactly = 1) { viewModel.onPreviewStopped() }
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.LARGE) }
            verify(exactly = 1) { viewModel.onConfirm() }
            verify(exactly = 1) { onDismiss() }
            confirmVerified(viewModel, onDismiss)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `キャンセルは破棄とダイアログを閉じる操作を通知する`() {
        // DisposableEffectのキー比較はUI操作の検証対象から除外する。
        excludeRecords { viewModel.equals(viewModel) }
        every { viewModel.onPreviewStopped() } returns Unit
        every { viewModel.uiState } returns MutableStateFlow(OtherOverlayTextSizeDetailUiState())
        every { viewModel.onDismiss() } returns Unit
        every { onDismiss() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            var showDialog by mutableStateOf(true)
            rule.setContent {
                if (showDialog) {
                    OtherOverlayTextSizeDetailDialog(onDismiss)
                }
            }
            rule.onNodeWithText("キャンセル").performClick()
            showDialog = false
            rule.waitForIdle()
            verify(exactly = 1) { viewModel.onPreviewStopped() }
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.onDismiss() }
            verify(exactly = 0) { viewModel.onConfirm() }
            verify(exactly = 1) { onDismiss() }
            confirmVerified(viewModel, onDismiss)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `ダイアログがCompositionから外れるとプレビュー解除を通知する`() {
        // DisposableEffectのキー比較はUI操作の検証対象から除外する。
        excludeRecords { viewModel.equals(viewModel) }
        every { viewModel.uiState } returns MutableStateFlow(OtherOverlayTextSizeDetailUiState())
        every { viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.LARGE) } returns Unit
        every { viewModel.onPreviewStopped() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            var showDialog by mutableStateOf(true)
            rule.setContent {
                if (showDialog) {
                    OtherOverlayTextSizeDetailDialog(onDismiss)
                }
            }
            rule.onNodeWithText("大").performScrollTo().performClick()
            showDialog = false
            rule.waitForIdle()

            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.LARGE) }
            verify(exactly = 1) { viewModel.onPreviewStopped() }
            verify(exactly = 0) { viewModel.onDismiss() }
            verify(exactly = 0) { viewModel.onConfirm() }
            verify(exactly = 0) { onDismiss() }
            confirmVerified(viewModel, onDismiss)
        } finally {
            stopKoin()
        }
    }
}
