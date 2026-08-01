package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

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

class Gt7Ps5ReadoutMyBestLapDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト状態の説明と通常音声チップを表示する`() {
        rule.setContent {
            MaterialTheme {
                Gt7Ps5ReadoutMyBestLapDetailPaneContent()
            }
        }

        rule.onNodeWithText("自己ベストラップを更新したときに音声でお知らせします。").assertIsDisplayed()
        rule
            .onAllNodesWithText("自己ベストラップ更新")[1]
            .assertIsDisplayed()
            .assertIsSelected()
        rule.onNodeWithText("ベストラップ").assertIsDisplayed()
    }

    @Test
    fun `チップをタップすると音声種別変更とプレビューが呼ばれる`() {
        var changedVoiceType: MyBestLapVoiceType? = null
        var previewedVoiceType: MyBestLapVoiceType? = null
        rule.setContent {
            MaterialTheme {
                Gt7Ps5ReadoutMyBestLapDetailPaneContent(
                    onVoiceTypeChanged = { changedVoiceType = it },
                    onPreviewClicked = { previewedVoiceType = it },
                )
            }
        }

        rule.onNodeWithText("ベストラップ").performClick()

        assertEquals(MyBestLapVoiceType.CASUAL, changedVoiceType)
        assertEquals(MyBestLapVoiceType.CASUAL, previewedVoiceType)
    }
}
