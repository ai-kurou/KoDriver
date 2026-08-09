package kurou.kodriver.feature.telemetryloglist

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * listPane の長押しメニュー・削除確認ダイアログ・削除結果スナックバーを検証するテスト。
 * [TelemetryLogContentTest] のテストケース数が detekt の TooManyFunctions 閾値を超えないよう分離している。
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class TelemetryLogDeleteFlowContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `その他ボタンをタップしてフィードバック送信メニューを選ぶとonFeedbackClickにログIDを渡す`() {
        var clickedLogId: Long? = null
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState =
                    TelemetryLogListUiState(
                        logs = listOf(createTelemetryLog(id = 1)),
                    ),
                onFeedbackClick = { clickedLogId = it },
            )
        }

        rule.onNodeWithContentDescription("その他の操作").performClick()
        rule.onNodeWithText("フィードバック送信").performClick()

        assertEquals(1L, clickedLogId)
    }

    @Test
    fun `その他ボタンをタップして削除メニューを選ぶとonDeleteClickにログIDを渡す`() {
        var clickedLogId: Long? = null
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState =
                    TelemetryLogListUiState(
                        logs = listOf(createTelemetryLog(id = 1)),
                    ),
                onDeleteClick = { clickedLogId = it },
            )
        }

        rule.onNodeWithContentDescription("その他の操作").performClick()
        rule.onNodeWithText("削除").performClick()

        assertEquals(1L, clickedLogId)
    }

    @Test
    fun `アイテムを長押しするとメニューを表示する`() {
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState =
                    TelemetryLogListUiState(
                        logs = listOf(createTelemetryLog(id = 1)),
                    ),
            )
        }

        rule.onNodeWithText("フラッグ").performTouchInput { longClick() }

        rule.onNodeWithText("フィードバック送信").assertExists()
        rule.onNodeWithText("削除").assertExists()
    }

    @Test
    fun `削除確認ダイアログの実行ボタンをタップするとonDeleteConfirmを呼ぶ`() {
        var confirmed = false
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(pendingDeleteLogId = 2L),
                onDeleteConfirm = { confirmed = true },
            )
        }

        rule.onNodeWithText("削除する").performClick()

        assertTrue(confirmed)
    }

    @Test
    fun `削除確認ダイアログのキャンセルボタンをタップするとonDeleteDismissを呼ぶ`() {
        var dismissed = false
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(pendingDeleteLogId = 2L),
                onDeleteDismiss = { dismissed = true },
            )
        }

        rule.onNodeWithText("キャンセル").performClick()

        assertTrue(dismissed)
    }

    @Test
    fun `deleteSucceededがtrueになるとスナックバーで成功を通知しconsumeする`() {
        var consumed = false
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(deleteSucceeded = true),
                onDeleteResultConsumed = { consumed = true },
            )
        }

        rule.onNodeWithText("ログを削除しました").assertExists()
        rule.waitUntil { consumed }
    }

    @Test
    fun `deleteSucceededがfalseになるとスナックバーで失敗を通知する`() {
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(deleteSucceeded = false),
            )
        }

        rule.onNodeWithText("ログの削除に失敗しました").assertExists()
    }
}
