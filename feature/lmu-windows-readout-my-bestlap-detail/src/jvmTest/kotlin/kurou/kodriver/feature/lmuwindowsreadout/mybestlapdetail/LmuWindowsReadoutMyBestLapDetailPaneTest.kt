package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.domain.model.MyBestLapVoiceType
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutMyBestLapDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト状態の説明と通常音声チップを表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutMyBestLapDetailPaneContent()
            }
        }

        rule.onNodeWithText("自己ベストラップを更新したときに音声でお知らせします。").assertIsDisplayed()
        rule.onNodeWithText("設定").assertIsDisplayed()
        rule.onAllNodesWithText("自己ベストラップ更新")[1]
            .assertIsDisplayed()
            .assertIsSelected()
        rule.onNodeWithText("自己ベストラップを更新したぞ").assertIsDisplayed()
    }

    @Test
    fun `カジュアル音声を選択状態で表示できる`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutMyBestLapDetailPaneContent(
                    uiState = LmuWindowsReadoutMyBestLapDetailUiState(voiceType = MyBestLapVoiceType.CASUAL),
                )
            }
        }

        rule.onNodeWithText("自己ベストラップを更新したぞ")
            .assertIsDisplayed()
            .assertIsSelected()
    }

    @Test
    fun `チップをタップするとonVoiceTypeChangedとonPreviewClickedが呼ばれる`() {
        var changedVoiceType: MyBestLapVoiceType? = null
        var previewedVoiceType: MyBestLapVoiceType? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutMyBestLapDetailPaneContent(
                    onVoiceTypeChanged = { changedVoiceType = it },
                    onPreviewClicked = { previewedVoiceType = it },
                )
            }
        }

        rule.onNodeWithText("自己ベストラップを更新したぞ").performClick()

        assertEquals(MyBestLapVoiceType.CASUAL, changedVoiceType)
        assertEquals(MyBestLapVoiceType.CASUAL, previewedVoiceType)
    }
}
