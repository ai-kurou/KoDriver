package kurou.kodriver.feature.telemetryloglist

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.hasContentDescription
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
    fun `ログをタップするとonLogClickにログIDを渡す`() {
        var clickedLogId: Long? = null
        rule.setContent {
            DetailPane(
                uiState = TelemetryLogListUiState(logs = listOf(createTelemetryLog(id = 1))),
                onLogClick = { clickedLogId = it },
            )
        }

        rule.onNodeWithText("フラッグ").performClick()

        assertEquals(1L, clickedLogId)
    }

    @Test
    fun `ログを選択中はボトムシートにログ詳細を表示する`() {
        rule.setContent {
            DetailPane(
                uiState =
                    TelemetryLogListUiState(
                        logs = listOf(createTelemetryLog(id = 1)),
                        selectedLogId = 1L,
                    ),
                detailContent = { id -> Text("詳細 $id") },
            )
        }

        rule.onNodeWithText("詳細 1").assertExists()
    }

    @Test
    fun `ログを選択していない場合はログ詳細を表示しない`() {
        rule.setContent {
            DetailPane(
                uiState = TelemetryLogListUiState(logs = listOf(createTelemetryLog(id = 1))),
                detailContent = { id -> Text("詳細 $id") },
            )
        }

        rule.onNodeWithText("詳細 1").assertDoesNotExist()
    }

    @Test
    fun `ログを長押ししてもメニューを開かない`() {
        rule.setContent {
            DetailPane(uiState = TelemetryLogListUiState(logs = listOf(createTelemetryLog(id = 1))))
        }

        rule.onNodeWithText("フラッグ").performTouchInput { longClick() }

        rule.onNodeWithText("フィードバック送信").assertDoesNotExist()
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
    onLogClick: (Long) -> Unit = {},
    onFeedbackClick: (Long) -> Unit = {},
    detailContent: @Composable (Long) -> Unit = {},
    onDeleteClick: (Long) -> Unit = {},
    onDeleteConfirm: () -> Unit = {},
    onDeleteDismiss: () -> Unit = {},
    onDeleteResultConsumed: () -> Unit = {},
) {
    TelemetryLogListDetailPaneContent(
        uiState = uiState,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        onLogClick = onLogClick,
        onLogDetailDismiss = {},
        onDeleteClick = onDeleteClick,
        onDeleteConfirm = onDeleteConfirm,
        onDeleteDismiss = onDeleteDismiss,
        onDeleteResultConsumed = onDeleteResultConsumed,
        onFeedbackClick = onFeedbackClick,
        detailContent = detailContent,
    )
}
