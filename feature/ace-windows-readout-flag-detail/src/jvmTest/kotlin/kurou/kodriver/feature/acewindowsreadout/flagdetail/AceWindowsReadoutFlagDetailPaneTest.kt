package kurou.kodriver.feature.acewindowsreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AceWindowsReadoutFlagDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文とブルーフラッグカードが表示される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutFlagDetailPaneContent()
            }
        }

        rule
            .onNodeWithText(
            "ホワイトフラッグ・グリーンフラッグ・レッドフラッグ・イエローフラッグなどのフラッグ状況を音声でお知らせします。",
        ).assertIsDisplayed()
        rule.onAllNodesWithText("ブルーフラッグ")[0].assertIsDisplayed()
    }

    @Test
    fun `フラッグカードをタップするとonFlagEnabledChangedが呼ばれる`() {
        var changedItem: FlagReadoutItem? = null
        var changedEnabled: Boolean? = null
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutFlagDetailPaneContent(
                    uiState = AceWindowsReadoutFlagDetailUiState(),
                    onFlagEnabledChanged = { item, enabled ->
                        changedItem = item
                        changedEnabled = enabled
                    },
                    onPreviewClicked = {},
                )
            }
        }

        rule.onAllNodesWithText("ブルーフラッグ")[0].assertIsDisplayed().performClick()

        assertEquals(FlagReadoutItem.BlueFlag, changedItem)
        assertEquals(false, changedEnabled)
    }

    @Test
    fun `フラッグチップをタップするとonPreviewClickedが呼ばれる`() {
        var previewedItem: FlagReadoutItem? = null
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutFlagDetailPaneContent(
                    uiState = AceWindowsReadoutFlagDetailUiState(),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = { previewedItem = it },
                )
            }
        }

        rule.onAllNodesWithText("ブルーフラッグ")[1].performClick()

        assertEquals(FlagReadoutItem.BlueFlag, previewedItem)
    }
}
