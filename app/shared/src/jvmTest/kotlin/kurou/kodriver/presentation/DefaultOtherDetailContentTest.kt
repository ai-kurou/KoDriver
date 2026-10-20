package kurou.kodriver.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.feature.otherlist.OtherListItemType
import org.junit.Rule
import org.junit.Test

class DefaultOtherDetailContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `音声の用途を選択すると説明詳細を表示する`() {
        rule.setContent {
            MaterialTheme {
                DefaultOtherDetailContent(
                    itemType = OtherListItemType.AndroidAudioUsage,
                    selectedSimulatorId = "",
                    canNavigateBack = false,
                    onBack = {},
                    feedbackTelemetryLogId = null,
                    feedbackAttachRequestId = 0L,
                )
            }
        }

        rule.onNodeWithText("音声の用途").assertIsDisplayed()
        rule.onNodeWithText("読み上げ音声を、メディアや通話などどの用途の音声として扱うか設定します。").assertIsDisplayed()
    }
}
