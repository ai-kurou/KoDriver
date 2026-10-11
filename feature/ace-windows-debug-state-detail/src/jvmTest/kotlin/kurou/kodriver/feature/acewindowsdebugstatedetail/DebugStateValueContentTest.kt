package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.ExtendedColorScheme
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DebugStateValueContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `温度の値と欠損値と状態チップは文字列を維持する`() {
        rule.setContent {
            MaterialTheme {
                Column(Modifier.width(360.dp)) {
                    DebugStateHeatTile(text = "FL 69.0℃", celsius = 69.0)
                    DebugStateHeatTile(text = "FR 85.0℃", celsius = 85.0)
                    DebugStateHeatTile(text = "RL 100.0℃", celsius = 100.0)
                    DebugStateHeatTile(text = "RR 110.0℃", celsius = 110.0)
                    DebugStateHeatTile(text = "FL -℃", celsius = null)
                    DebugStateStatusChip(text = "グリーンフラッグ")
                }
            }
        }

        listOf(
            "FL 69.0℃",
            "FR 85.0℃",
            "RL 100.0℃",
            "RR 110.0℃",
            "FL -℃",
            "グリーンフラッグ",
        ).forEach { text -> rule.onNodeWithText(text).assertIsDisplayed() }
    }

    @Test
    fun `ヒートレベルはテーマの拡張色に対応する`() {
        val actual = mutableMapOf<HeatLevel, Color>()
        var expected: ExtendedColorScheme? = null
        rule.setContent {
            KoDriverTheme {
                expected = KoDriverExtendedColors.current
                HeatLevel.entries.forEach { actual[it] = heatColor(it) }
            }
        }
        rule.waitForIdle()

        assertEquals(expected?.heatCool, actual[HeatLevel.COOL])
        assertEquals(expected?.heatOk, actual[HeatLevel.OK])
        assertEquals(expected?.heatWarm, actual[HeatLevel.WARM])
        assertEquals(expected?.heatHot, actual[HeatLevel.HOT])
    }
}
