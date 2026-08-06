package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.domain.model.MyBestLapVoiceType
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutMyBestLapDetailPaneTest {
    @Test
    fun `デフォルト状態の説明と通常音声チップを表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    LmuWindowsReadoutMyBestLapDetailPaneContent()
                }
            }

            onNodeWithText("自己ベストラップを更新したときに音声でお知らせします。").assertIsDisplayed()
            onAllNodesWithText("自己ベストラップ更新")[1]
                .assertIsDisplayed()
                .assertIsSelected()
            onNodeWithText("ベストラップ").assertIsDisplayed()
        }

    @Test
    fun `カジュアル音声を選択状態で表示できる`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    LmuWindowsReadoutMyBestLapDetailPaneContent(
                        uiState = LmuWindowsReadoutMyBestLapDetailUiState(voiceType = MyBestLapVoiceType.CASUAL),
                    )
                }
            }

            onNodeWithText("ベストラップ")
                .assertIsDisplayed()
                .assertIsSelected()
        }

    @Test
    fun `チップをタップするとonVoiceTypeChangedとonPreviewClickedが呼ばれる`() =
        composeScreenshotTest {
            var changedVoiceType: MyBestLapVoiceType? = null
            var previewedVoiceType: MyBestLapVoiceType? = null
            setContent {
                MaterialTheme {
                    LmuWindowsReadoutMyBestLapDetailPaneContent(
                        onVoiceTypeChanged = { changedVoiceType = it },
                        onPreviewClicked = { previewedVoiceType = it },
                    )
                }
            }

            onNodeWithText("ベストラップ").performClick()

            assertEquals(MyBestLapVoiceType.CASUAL, changedVoiceType)
            assertEquals(MyBestLapVoiceType.CASUAL, previewedVoiceType)
        }
}
