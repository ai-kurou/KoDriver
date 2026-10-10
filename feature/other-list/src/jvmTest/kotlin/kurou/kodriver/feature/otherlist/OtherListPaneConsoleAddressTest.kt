package kurou.kodriver.feature.otherlist

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Rule
import org.junit.Test

class OtherListPaneConsoleAddressTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ゲーム機のIPアドレスと直接接続のポートを表示する`() {
        assertConsoleAddressLabel("192.168.1.100", 33740, "192.168.1.100:33740")
    }

    @Test
    fun `SimHubのIPアドレスとポートを表示する`() {
        assertConsoleAddressLabel("192.168.1.100", 33741, "192.168.1.100:33741")
    }

    @Test
    fun `IPアドレスがない場合はポートを表示せず未設定を表示する`() {
        assertConsoleAddressLabel(null, 33740, "未設定")
    }

    @Test
    fun `IPアドレスが空の場合は未設定を表示する`() {
        assertConsoleAddressLabel("", 33741, "未設定")
    }

    private fun assertConsoleAddressLabel(
        consoleAddress: String?,
        consolePort: Int,
        label: String,
    ) {
        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.Voice, OtherListItemType.ConsoleIp),
                        consoleAddress = consoleAddress,
                        consolePort = consolePort,
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
        rule.onNode(hasText("ゲーム機・SimHubへ接続するIPアドレス") and hasText(label)).assertIsDisplayed()
        rule.onNode(hasText("読み上げ音声") and hasText(label)).assertDoesNotExist()
    }
}
