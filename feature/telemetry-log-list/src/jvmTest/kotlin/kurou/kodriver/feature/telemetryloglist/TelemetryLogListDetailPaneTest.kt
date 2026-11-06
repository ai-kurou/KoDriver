package kurou.kodriver.feature.telemetryloglist

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * ログ一覧だけを詳細ペインとして表示する [TelemetryLogListDetailPaneContent] のテスト。
 */
class TelemetryLogListDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルとログ一覧を表示する`() {
        rule.setContent {
            DetailPane(uiState = TelemetryLogListUiState(logs = listOf(createTelemetryLog(id = 1))))
        }

        rule.onNodeWithText("ログ").assertExists()
        rule.onNodeWithText("フラッグ").assertExists()
    }

    @Test
    fun `ログが0件の場合は空状態を表示する`() {
        rule.setContent { DetailPane(uiState = TelemetryLogListUiState()) }

        rule.onNodeWithText("ログはまだありません").assertExists()
    }

    @Test
    fun `ログをタップしても何も起きない`() {
        var feedbackLogId: Long? = null
        var deleteLogId: Long? = null
        rule.setContent {
            DetailPane(
                uiState = TelemetryLogListUiState(logs = listOf(createTelemetryLog(id = 1))),
                onFeedbackClick = { feedbackLogId = it },
                onDeleteClick = { deleteLogId = it },
            )
        }

        rule.onNodeWithText("フラッグ").performClick()

        assertEquals(null, feedbackLogId)
        assertEquals(null, deleteLogId)
        rule.onNodeWithText("フラッグ").assertExists()
    }

    @Test
    fun `戻れる場合は戻るボタンでonBackを呼ぶ`() {
        var backed = false
        rule.setContent {
            DetailPane(
                uiState = TelemetryLogListUiState(),
                canNavigateBack = true,
                onBack = { backed = true },
            )
        }

        rule.onNode(hasContentDescription("戻る")).performClick()

        assertTrue(backed)
    }

    @Test
    fun `戻れない場合は戻るボタンを表示しない`() {
        rule.setContent { DetailPane(uiState = TelemetryLogListUiState(), canNavigateBack = false) }

        rule.onNode(hasContentDescription("戻る")).assertDoesNotExist()
    }

    @Test
    fun `その他ボタンからフィードバック送信を選ぶとonFeedbackClickにログIDを渡す`() {
        var clickedLogId: Long? = null
        rule.setContent {
            DetailPane(
                uiState = TelemetryLogListUiState(logs = listOf(createTelemetryLog(id = 1))),
                onFeedbackClick = { clickedLogId = it },
            )
        }

        rule.onNodeWithContentDescription("その他の操作").performClick()
        rule.onNodeWithText("フィードバック送信").performClick()

        assertEquals(1L, clickedLogId)
    }

    @Test
    fun `その他ボタンから削除を選ぶとonDeleteClickにログIDを渡す`() {
        var clickedLogId: Long? = null
        rule.setContent {
            DetailPane(
                uiState = TelemetryLogListUiState(logs = listOf(createTelemetryLog(id = 1))),
                onDeleteClick = { clickedLogId = it },
            )
        }

        rule.onNodeWithContentDescription("その他の操作").performClick()
        rule.onNodeWithText("削除").performClick()

        assertEquals(1L, clickedLogId)
    }

    @Test
    fun `削除確認ダイアログの実行ボタンでonDeleteConfirmを呼ぶ`() {
        var confirmed = false
        rule.setContent {
            DetailPane(
                uiState = previewTelemetryLogListUiState.copy(pendingDeleteLogId = 2L),
                onDeleteConfirm = { confirmed = true },
            )
        }

        rule.onNodeWithText("削除する").performClick()

        assertTrue(confirmed)
    }

    @Test
    fun `削除確認ダイアログのキャンセルボタンでonDeleteDismissを呼ぶ`() {
        var dismissed = false
        rule.setContent {
            DetailPane(
                uiState = previewTelemetryLogListUiState.copy(pendingDeleteLogId = 2L),
                onDeleteDismiss = { dismissed = true },
            )
        }

        rule.onNodeWithText("キャンセル").performClick()

        assertTrue(dismissed)
    }

    @Test
    fun `削除成功をスナックバーで通知しconsumeする`() {
        var consumed = false
        rule.setContent {
            DetailPane(
                uiState = previewTelemetryLogListUiState.copy(deleteSucceeded = true),
                onDeleteResultConsumed = { consumed = true },
            )
        }

        rule.onNodeWithText("ログを削除しました").assertExists()
        rule.waitUntil(timeoutMillis = 5_000L) { consumed }
    }

    @Test
    fun `削除失敗をスナックバーで通知する`() {
        rule.setContent {
            DetailPane(uiState = previewTelemetryLogListUiState.copy(deleteSucceeded = false))
        }

        rule.onNodeWithText("ログの削除に失敗しました").assertExists()
    }
}

@Composable
private fun DetailPane(
    uiState: TelemetryLogListUiState,
    canNavigateBack: Boolean = false,
    onBack: () -> Unit = {},
    onFeedbackClick: (Long) -> Unit = {},
    onDeleteClick: (Long) -> Unit = {},
    onDeleteConfirm: () -> Unit = {},
    onDeleteDismiss: () -> Unit = {},
    onDeleteResultConsumed: () -> Unit = {},
) {
    TelemetryLogListDetailPaneContent(
        uiState = uiState,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        onDeleteClick = onDeleteClick,
        onDeleteConfirm = onDeleteConfirm,
        onDeleteDismiss = onDeleteDismiss,
        onDeleteResultConsumed = onDeleteResultConsumed,
        onFeedbackClick = onFeedbackClick,
    )
}
