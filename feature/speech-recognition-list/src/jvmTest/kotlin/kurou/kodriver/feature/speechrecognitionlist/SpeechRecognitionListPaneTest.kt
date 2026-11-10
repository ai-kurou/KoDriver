package kurou.kodriver.feature.speechrecognitionlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.SpeechCommandGuideGroup
import kurou.kodriver.domain.model.SpeechCommandGuideItem
import org.junit.Rule
import org.junit.Test

class SpeechRecognitionListPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private val fuelGroup =
        SpeechCommandGuideGroup(
            category = ManualReadoutItemCategory.FUEL,
            items = listOf(SpeechCommandGuideItem("fuel", "燃料残量", listOf("フューエル", "ガス"))),
        )

    @Test
    fun `項目がなくても呼びかけの案内と空の案内を表示する`() {
        rule.setContent { KoDriverTheme { SpeechRecognitionListPaneContent(SpeechRecognitionListUiState()) } }

        rule.onNodeWithTag(SPEECH_RECOGNITION_LIST_PANE_TEST_TAG).assertIsDisplayed()
        rule.onNodeWithText("最初に「コドライバー」と呼びかけます。").assertIsDisplayed()
        rule.onNodeWithTag(SPEECH_RECOGNITION_LIST_EMPTY_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun `種類の見出しと項目名と語句をすべて表示し空の案内は出さない`() {
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionListPaneContent(
                    SpeechRecognitionListUiState(
                        groups =
                            listOf(
                                fuelGroup,
                                SpeechCommandGuideGroup(
                                    ManualReadoutItemCategory.VIRTUAL_ENERGY,
                                    listOf(SpeechCommandGuideItem("ve", "VE残量", listOf("エネルギー"))),
                                ),
                                SpeechCommandGuideGroup(
                                    ManualReadoutItemCategory.TYRE,
                                    listOf(SpeechCommandGuideItem("tyre", "タイヤ温度", listOf("ゴム"))),
                                ),
                                SpeechCommandGuideGroup(
                                    ManualReadoutItemCategory.BRAKE,
                                    listOf(SpeechCommandGuideItem("brake", "ブレーキ温度", listOf("ディスク"))),
                                ),
                                SpeechCommandGuideGroup(
                                    ManualReadoutItemCategory.LAP,
                                    listOf(SpeechCommandGuideItem("lap", "ベストラップ", listOf("ベスト"))),
                                ),
                            ),
                    ),
                )
            }
        }

        listOf("燃料", "タイヤ", "ブレーキ", "ラップ", "燃料残量", "フューエル", "ガス", "ゴム", "ディスク", "バーチャルエネルギー", "VE残量", "ベストラップ")
            .forEach { rule.onNodeWithText(it, useUnmergedTree = true).assertExists() }
        rule.onNodeWithTag(SPEECH_RECOGNITION_LIST_EMPTY_TEST_TAG).assertDoesNotExist()
    }
}
