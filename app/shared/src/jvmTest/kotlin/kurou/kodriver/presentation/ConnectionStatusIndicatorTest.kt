package kurou.kodriver.presentation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class ConnectionStatusIndicatorTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `接続確認前は表示しない`() {
        composeRule.setContent {
            KoDriverTheme {
                ConnectionStatusIndicator(status = ConnectionStatus.Hidden)
            }
        }

        composeRule.onAllNodesWithTag(CONNECTION_STATUS_TEST_TAG).assertCountEquals(0)
    }

    @Test
    fun `未接続時は表示する`() {
        composeRule.setContent {
            KoDriverTheme {
                ConnectionStatusIndicator(status = ConnectionStatus.Waiting)
            }
        }

        composeRule.onNodeWithTag(CONNECTION_STATUS_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun `接続時は表示する`() {
        composeRule.setContent {
            KoDriverTheme {
                ConnectionStatusIndicator(status = ConnectionStatus.Connected)
            }
        }

        composeRule.onNodeWithTag(CONNECTION_STATUS_TEST_TAG).assertIsDisplayed()
    }
}
