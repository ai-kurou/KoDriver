package kurou.kodriver.feature.otherlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.AndroidAudioUsage
import org.junit.Rule
import org.junit.Test

class OtherListPaneAndroidAudioUsageTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `音声の用途の現在値を全用途で表示する`() {
        var state by mutableStateOf(OtherListUiState(items = listOf(OtherListItemType.AndroidAudioUsage)))
        rule.setContent {
            KoDriverTheme {
                OtherListPane(
                    uiState = state,
                    onItemClick = {},
                    onOverlayVisibleChange = {},
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                    onHapticFeedbackEnabledChange = {},
                    onStartupEnabledChange = {},
                )
            }
        }
        rule.onNodeWithText("メディア").assertIsDisplayed()
        val labels = listOf("メディア", "通話", "着信音", "通知", "アラーム", "アシスタント")
        AndroidAudioUsage.entries.forEachIndexed { index, usage ->
            rule.runOnIdle { state = state.copy(androidAudioUsage = usage) }
            rule.onNodeWithText(labels[index]).assertIsDisplayed()
        }
    }
}
