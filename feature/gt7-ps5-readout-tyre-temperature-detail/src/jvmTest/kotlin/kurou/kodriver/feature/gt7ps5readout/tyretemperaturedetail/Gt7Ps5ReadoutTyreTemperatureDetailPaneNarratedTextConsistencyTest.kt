package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.engine.Gt7Ps5TyreOverheat
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

/**
 * UI の文言入力欄ラベルと、試聴サンプルの温度を展開した TTS 記録文言を検証する。
 * 表示文言と音声の記録文言をそれぞれ検証する。
 */
class Gt7Ps5ReadoutTyreTemperatureDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `文言入力欄はタイヤ過熱警告の文言を表示し試聴文言にはサンプルの温度を含む`() {
        rule.setContent {
            MaterialTheme {
                Gt7Ps5ReadoutTyreTemperatureDetailPaneContent()
            }
        }

        rule.onNodeWithText("タイヤ過熱警告の文言").assertIsDisplayed()
        assertEquals("タイヤ過熱 95度", Gt7Ps5TyreOverheat(95).narratedText)
    }
}
