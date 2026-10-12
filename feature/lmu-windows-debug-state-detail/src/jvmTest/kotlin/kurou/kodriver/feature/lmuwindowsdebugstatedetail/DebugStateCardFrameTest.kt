package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DebugStateCardFrameTest(
    private val cardKey: LmuWindowsDebugStateCardKey,
) {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `全カードのフレームはタイトルとアイコンと内容を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateCardFrame(cardKey = cardKey, title = cardKey.name) {
                    Text(text = "カード内容")
                }
            }
        }

        rule.onNodeWithText(cardKey.name).assertIsDisplayed()
        // 装飾アイコンはスクリーンリーダーの読み上げ対象に含めない。
        rule.onNodeWithText(debugStateCardSymbol(cardKey)).assertDoesNotExist()
        rule.onNodeWithText("カード内容").assertIsDisplayed()
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cardKeys(): List<Array<LmuWindowsDebugStateCardKey>> =
            LmuWindowsDebugStateCardKey.entries.map { arrayOf(it) }
    }
}
