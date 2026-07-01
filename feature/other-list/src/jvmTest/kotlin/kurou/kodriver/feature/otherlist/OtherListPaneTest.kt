package kurou.kodriver.feature.otherlist

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherListPaneTest {

    @get:Rule
    val rule = createComposeRule()

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
}
