package kurou.kodriver.feature.speechrecognitionlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.SpeechCommandGuideGroup
import kurou.kodriver.domain.model.SpeechCommandGuideItem
import org.junit.Rule
import org.junit.Test

class SpeechRecognitionListPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private companion object {
        const val LONGEST_PHRASE_NOTE =
            "語句の一部だけでなく、できるだけ最後まではっきり言ってください。「タイヤ残り」より「タイヤ残り周回」のように、長い語句のほうが優先されます。"
    }

    private val fuelGroup =
        SpeechCommandGuideGroup(
            category = ManualReadoutItemCategory.FUEL,
            items = listOf(SpeechCommandGuideItem("fuel", "燃料残量", listOf("残り燃料", "ガス"))),
        )

    @Test
    fun `項目がなくても呼びかけの案内を表示する`() {
        rule.setContent { KoDriverTheme { SpeechRecognitionListPaneContent(SpeechRecognitionListUiState()) } }

        rule.onNodeWithTag(SPEECH_RECOGNITION_LIST_PANE_TEST_TAG).assertIsDisplayed()
        rule.onNodeWithText("まずはこう呼びかけます").assertIsDisplayed()
        rule.onNodeWithText("「コドライバー」").assertIsDisplayed()
        listOf("確認音", "下の語句を言う", "読み上げ").forEach { rule.onNodeWithText(it).assertIsDisplayed() }
        rule.onNodeWithText(LONGEST_PHRASE_NOTE).assertIsDisplayed()
    }

    @Test
    fun `種類の見出しと項目名と語句をすべて表示する`() {
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

        listOf("燃料", "タイヤ", "ブレーキ", "ラップ", "燃料残量", "残り燃料", "ガス", "ゴム", "ディスク", "バーチャルエネルギー", "VE残量", "ベストラップ")
            .forEach { rule.onNodeWithText(it, useUnmergedTree = true).assertExists() }
    }

    @Test
    fun `項目のカードに語句の数を表示する`() {
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionListPaneContent(
                    SpeechRecognitionListUiState(groups = listOf(fuelGroup)),
                )
            }
        }

        rule.onNodeWithText("2語", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `読み上げられた項目が画面外なら、その項目まで自動でスクロールする`() {
        val items = (1..30).map { SpeechCommandGuideItem("item-$it", "項目$it", listOf("語句$it")) }
        val groups = listOf(SpeechCommandGuideGroup(ManualReadoutItemCategory.FUEL, items))
        var uiState by mutableStateOf(SpeechRecognitionListUiState(groups = groups))
        rule.setContent { KoDriverTheme { SpeechRecognitionListPaneContent(uiState) } }
        rule.onNodeWithText("語句30", useUnmergedTree = true).assertDoesNotExist()

        uiState = uiState.copy(highlight = SpeechPhraseHighlight("item-30", "語句30", 1))
        rule.waitForIdle()

        rule.onNodeWithText("語句30", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `読み上げられた項目が既に見えているなら、スクロールせず強調だけ行う`() {
        var uiState by mutableStateOf(SpeechRecognitionListUiState(groups = listOf(fuelGroup)))
        rule.setContent { KoDriverTheme { SpeechRecognitionListPaneContent(uiState) } }

        uiState = uiState.copy(highlight = SpeechPhraseHighlight("fuel", "ガス", 1))
        rule.waitForIdle()

        rule.onNodeWithText("まずはこう呼びかけます").assertIsDisplayed()
        rule.onNodeWithText("ガス", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `案内にない項目の強調指示では何も起きない`() {
        var uiState by mutableStateOf(SpeechRecognitionListUiState(groups = listOf(fuelGroup)))
        rule.setContent { KoDriverTheme { SpeechRecognitionListPaneContent(uiState) } }

        uiState = uiState.copy(highlight = SpeechPhraseHighlight("unknown", "未登録", 1))
        rule.waitForIdle()

        rule.onNodeWithText("まずはこう呼びかけます").assertIsDisplayed()
    }

    @Test
    fun `強調の指示を外すと元の表示に戻る`() {
        var uiState by mutableStateOf(SpeechRecognitionListUiState(groups = listOf(fuelGroup)))
        rule.setContent { KoDriverTheme { SpeechRecognitionListPaneContent(uiState) } }

        uiState = uiState.copy(highlight = SpeechPhraseHighlight("fuel", "ガス", 1))
        rule.waitForIdle()
        uiState = uiState.copy(highlight = null)
        rule.waitForIdle()

        rule.onNodeWithText("ガス", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `先頭側が見切れている項目や上へ通り過ぎた項目も、強調の指示で見える位置へ戻す`() {
        val items = (1..30).map { SpeechCommandGuideItem("item-$it", "項目$it", listOf("語句$it")) }
        val groups = listOf(SpeechCommandGuideGroup(ManualReadoutItemCategory.FUEL, items))
        var uiState by mutableStateOf(SpeechRecognitionListUiState(groups = groups))
        rule.setContent { KoDriverTheme { SpeechRecognitionListPaneContent(uiState) } }

        // 一覧を下へ送り、先頭付近の項目を画面の上へ通り過ぎさせる
        rule.onNodeWithTag(SPEECH_RECOGNITION_LIST_PANE_TEST_TAG).performTouchInput { swipeUp() }
        rule.waitForIdle()
        uiState = uiState.copy(highlight = SpeechPhraseHighlight("item-1", "語句1", 1))
        rule.waitForIdle()

        rule.onNodeWithText("語句1", useUnmergedTree = true).assertIsDisplayed()
    }
}
