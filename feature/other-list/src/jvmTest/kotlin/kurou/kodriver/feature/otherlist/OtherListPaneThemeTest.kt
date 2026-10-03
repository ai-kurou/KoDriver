package kurou.kodriver.feature.otherlist

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import kurou.kodriver.domain.model.ThemeMode
import org.junit.Rule
import org.junit.Test

class OtherListPaneThemeTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `テーマ項目にシステムに従うを表示する`() {
        assertThemeLabel(ThemeMode.SYSTEM, "システムに従う")
    }

    @Test
    fun `テーマ項目にライトを表示する`() {
        assertThemeLabel(ThemeMode.LIGHT, "ライト")
    }

    @Test
    fun `テーマ項目にダークを表示する`() {
        assertThemeLabel(ThemeMode.DARK, "ダーク")
    }

    private fun assertThemeLabel(
        themeMode: ThemeMode,
        label: String,
    ) {
        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.Voice, OtherListItemType.Theme),
                        themeMode = themeMode,
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
        rule.onNode(hasText("テーマ") and hasText(label)).assertIsDisplayed()
        rule.onNode(hasText("読み上げ音声") and hasText(label)).assertDoesNotExist()
    }
}
