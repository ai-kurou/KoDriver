package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Rule
import org.junit.Test

/**
 * 動的な温度を含む本文と独立した、過熱警告のChip表示を検証する。自由文言の入力UIはPR3で追加する。
 */
class AceWindowsReadoutTyreTemperatureDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `Chipに過熱警告のラベルを表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent()
            }
        }

        val narratedText = "タイヤ過熱警告"
        rule.onAllNodesWithText(narratedText)[0].assertTextContains(narratedText, substring = true)
    }
}
