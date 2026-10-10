package kurou.kodriver.feature.otherlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertNull

class OtherListPaneAccessLocalNetworkPermissionTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `許可状態の変更に追従して副テキストと設定案内を更新する`() {
        var granted by mutableStateOf(false)
        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.AccessLocalNetworkPermission),
                        accessLocalNetworkPermissionGranted = granted,
                    ),
                onItemClick = {},
                onOverlayVisibleChange = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }
        rule.onNodeWithText("許可されていません").assertExists()
        rule.onNodeWithText("タップして設定を開く").assertExists()
        rule.onNodeWithText("許可済み").assertDoesNotExist()

        rule.runOnIdle { granted = true }
        rule.onNodeWithText("許可済み").assertExists()
        rule.onNodeWithText("許可されていません").assertDoesNotExist()
        rule.onNodeWithText("タップして設定を開く").assertDoesNotExist()

        rule.runOnIdle { granted = false }
        rule.onNodeWithText("許可されていません").assertExists()
        rule.onNodeWithText("タップして設定を開く").assertExists()
        rule.onNodeWithText("許可済み").assertDoesNotExist()
    }

    @Test
    fun `ローカルネットワークへのアクセス許可項目をクリックしても項目クリックコールバックは呼ばない`() {
        var clickedItem: OtherListItemType? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.AccessLocalNetworkPermission),
                    ),
                onItemClick = { clickedItem = it },
                onOverlayVisibleChange = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }

        rule.onNode(hasText("ローカルネットワークへのアクセス許可")).performClick()

        assertNull(clickedItem)
    }
}
