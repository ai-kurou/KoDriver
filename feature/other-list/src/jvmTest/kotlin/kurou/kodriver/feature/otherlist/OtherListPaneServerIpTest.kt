package kurou.kodriver.feature.otherlist

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Rule
import org.junit.Test

class OtherListPaneServerIpTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `保存済みIPアドレスを表示する`() {
        assertServerIpLabel("192.168.1.100", "192.168.1.100")
    }

    @Test
    fun `IPアドレスがない場合は未設定を表示する`() {
        assertServerIpLabel(null, "未設定")
    }

    private fun assertServerIpLabel(
        serverIp: String?,
        label: String,
    ) {
        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.Voice, OtherListItemType.ServerIp),
                        serverIp = serverIp,
                    ),
                onItemClick = {},
                onOverlayVisibleChange = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }

        rule.onAllNodesWithText(label).assertCountEquals(1)
        rule.onNode(hasText("Windows版KoDriverへ接続するIPアドレス") and hasText(label)).assertIsDisplayed()
        rule.onNode(hasText("読み上げ音声") and hasText(label)).assertDoesNotExist()
    }
}
