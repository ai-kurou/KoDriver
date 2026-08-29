package kurou.kodriver.feature.otherlist

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertNull

class OtherListPaneAccessLocalNetworkPermissionTest {
    @get:Rule
    val rule = createComposeRule()

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
