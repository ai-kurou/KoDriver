package kurou.kodriver.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ConnectionBannerTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `CONNECTED状態でメッセージが表示される`() {
        composeRule.setContent {
            ConnectionBanner(
                uiState = ConnectionBannerUiState(
                    status = ConnectionBannerStatus.CONNECTED,
                    message = "接続済み",
                    iconType = ConnectionBannerIconType.NETWORK,
                ),
            )
        }

        composeRule.onNodeWithText("接続済み").assertIsDisplayed()
    }

    @Test
    fun `DISCONNECTED状態でメッセージが表示される`() {
        composeRule.setContent {
            ConnectionBanner(
                uiState = ConnectionBannerUiState(
                    status = ConnectionBannerStatus.DISCONNECTED,
                    message = "切断中",
                    iconType = ConnectionBannerIconType.NETWORK,
                ),
            )
        }

        composeRule.onNodeWithText("切断中").assertIsDisplayed()
    }

    @Test
    fun `UNCHECKED状態でメッセージが表示される`() {
        composeRule.setContent {
            ConnectionBanner(
                uiState = ConnectionBannerUiState(
                    status = ConnectionBannerStatus.UNCHECKED,
                    message = "確認中",
                    iconType = ConnectionBannerIconType.NETWORK,
                ),
            )
        }

        composeRule.onNodeWithText("確認中").assertIsDisplayed()
    }

    @Test
    fun `SIMULATORアイコンタイプかつCONNECTEDでメッセージが表示される`() {
        composeRule.setContent {
            ConnectionBanner(
                uiState = ConnectionBannerUiState(
                    status = ConnectionBannerStatus.CONNECTED,
                    message = "LMU接続済み",
                    iconType = ConnectionBannerIconType.SIMULATOR,
                ),
            )
        }

        composeRule.onNodeWithText("LMU接続済み").assertIsDisplayed()
    }

    @Test
    fun `SIMULATORアイコンタイプかつDISCONNECTEDでメッセージが表示される`() {
        composeRule.setContent {
            ConnectionBanner(
                uiState = ConnectionBannerUiState(
                    status = ConnectionBannerStatus.DISCONNECTED,
                    message = "LMU未接続",
                    iconType = ConnectionBannerIconType.SIMULATOR,
                ),
            )
        }

        composeRule.onNodeWithText("LMU未接続").assertIsDisplayed()
    }
}
