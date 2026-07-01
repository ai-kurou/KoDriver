package kurou.kodriver.feature.otherlist

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OtherListPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `通常項目をクリックすると項目クリックコールバックを呼ぶ`() {
        var clickedItem: OtherListItemType? = null

        rule.setContent {
            OtherListPane(
                uiState = OtherListUiState(
                    items = listOf(OtherListItemType.Volume),
                ),
                onItemClick = { clickedItem = it },
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
            )
        }

        rule.onNode(hasText("音量")).performClick()

        assertEquals(OtherListItemType.Volume, clickedItem)
    }

    @Test
    fun `画面スリープ無効をクリックすると切り替えコールバックを呼ぶ`() {
        var keepScreenOn: Boolean? = null

        rule.setContent {
            OtherListPane(
                uiState = OtherListUiState(
                    items = listOf(OtherListItemType.KeepScreenOn),
                    keepScreenOn = true,
                ),
                onItemClick = {},
                onKeepScreenOnChange = { keepScreenOn = it },
                onExitConfirmationEnabledChange = {},
            )
        }

        rule.onNode(hasText("画面をスリープさせない")).performClick()

        assertEquals(false, keepScreenOn)
    }

    @Test
    fun `画面スリープ無効がOFFのときにクリックするとONへ切り替えコールバックを呼ぶ`() {
        var keepScreenOn: Boolean? = null

        rule.setContent {
            OtherListPane(
                uiState = OtherListUiState(
                    items = listOf(OtherListItemType.KeepScreenOn),
                    keepScreenOn = false,
                ),
                onItemClick = {},
                onKeepScreenOnChange = { keepScreenOn = it },
                onExitConfirmationEnabledChange = {},
            )
        }

        rule.onNode(hasText("画面をスリープさせない")).performClick()

        assertEquals(true, keepScreenOn)
    }

    @Test
    fun `終了確認をクリックすると切り替えコールバックを呼ぶ`() {
        var exitConfirmationEnabled: Boolean? = null

        rule.setContent {
            OtherListPane(
                uiState = OtherListUiState(
                    items = listOf(OtherListItemType.ExitConfirmation),
                    exitConfirmationEnabled = true,
                ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = { exitConfirmationEnabled = it },
            )
        }

        rule.onNode(hasText("終了確認を表示")).performClick()

        assertEquals(false, exitConfirmationEnabled)
    }

    @Test
    fun `終了確認がOFFのときにクリックするとONへ切り替えコールバックを呼ぶ`() {
        var exitConfirmationEnabled: Boolean? = null

        rule.setContent {
            OtherListPane(
                uiState = OtherListUiState(
                    items = listOf(OtherListItemType.ExitConfirmation),
                    exitConfirmationEnabled = false,
                ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = { exitConfirmationEnabled = it },
            )
        }

        rule.onNode(hasText("終了確認を表示")).performClick()

        assertEquals(true, exitConfirmationEnabled)
    }

    @Test
    fun `Switch項目をクリックしても項目クリックコールバックは呼ばない`() {
        var clickedItem: OtherListItemType? = null

        rule.setContent {
            OtherListPane(
                uiState = OtherListUiState(
                    items = listOf(
                        OtherListItemType.KeepScreenOn,
                        OtherListItemType.ExitConfirmation,
                    ),
                ),
                onItemClick = { clickedItem = it },
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
            )
        }

        rule.onNode(hasText("画面をスリープさせない")).performClick()
        rule.onNode(hasText("終了確認を表示")).performClick()

        assertNull(clickedItem)
    }

    @Test
    fun `アプリバージョンが設定されているとバージョン行を表示する`() {
        rule.setContent {
            OtherListPane(
                uiState = OtherListUiState(
                    items = emptyList(),
                    appVersionLabel = "Android版KoDriverバージョン",
                    appVersion = "1.2.3",
                ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
            )
        }

        rule.onAllNodesWithText("Android版KoDriverバージョン").assertCountEquals(1)
        rule.onAllNodesWithText("1.2.3").assertCountEquals(1)
    }
}
