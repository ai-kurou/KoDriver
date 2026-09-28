package kurou.kodriver.feature.otherlist

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OtherListPaneTtsGuidanceTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `音声合成アプリをインストール項目をクリックすると項目クリックコールバックを呼ぶ`() {
        var clickedItem: OtherListItemType? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.TtsEngineMissing),
                    ),
                onItemClick = { clickedItem = it },
                onOverlayVisibleChange = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }

        rule.onNode(hasText("音声合成アプリをインストール")).performClick()

        assertEquals(OtherListItemType.TtsEngineMissing, clickedItem)
    }

    @Test
    fun `音声合成の日本語データを設定項目をクリックしても項目クリックコールバックは呼ばない`() {
        var clickedItem: OtherListItemType? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.TtsLanguageDataMissing),
                    ),
                onItemClick = { clickedItem = it },
                onOverlayVisibleChange = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }

        rule.onNode(hasText("音声合成の日本語データを設定")).performClick()

        assertNull(clickedItem)
    }
}
