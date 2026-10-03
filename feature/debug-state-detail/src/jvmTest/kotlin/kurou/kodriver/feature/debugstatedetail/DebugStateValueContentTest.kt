package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
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
    fun `温度と残溝の値と欠損値と状態チップは文字列を維持する`() {
        rule.setContent {
            MaterialTheme {
                Column(Modifier.width(360.dp)) {
                    DebugStateHeatTile(text = "FL 69.0℃", celsius = 69.0)
                    DebugStateHeatTile(text = "FR 85.0℃", celsius = 85.0)
                    DebugStateHeatTile(text = "RL 100.0℃", celsius = 100.0)
                    DebugStateHeatTile(text = "RR 110.0℃", celsius = 110.0)
                    DebugStateHeatTile(text = "FL -℃", celsius = null)
                    DebugStateWearMeter(text = "FL 80.0%", remainingPercent = 80.0)
                    DebugStateWearMeter(text = "FR 60.0%", remainingPercent = 60.0)
                    DebugStateWearMeter(text = "RL 20.0%", remainingPercent = 20.0)
                    DebugStateWearMeter(text = "RR -%", remainingPercent = null)
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
            "FL 80.0%",
            "FR 60.0%",
            "RL 20.0%",
            "RR -%",
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

    @Test
    fun `残溝が欠損のときはメーターの進捗を公開しない`() {
        rule.setContent {
            MaterialTheme {
                DebugStateWearMeter(text = "RR -%", remainingPercent = null)
            }
        }

        rule.onNodeWithText("RR -%").assertIsDisplayed()
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertCountEquals(0)
    }

    @Test
    fun `メーターは残溝割合を表示して範囲外の値を端に収める`() {
        rule.setContent {
            MaterialTheme {
                Column(Modifier.width(360.dp)) {
                    DebugStateWearMeter(text = "FL 80.0%", remainingPercent = 80.0)
                    DebugStateWearMeter(text = "FR 120.0%", remainingPercent = 120.0)
                    DebugStateWearMeter(text = "RL -10.0%", remainingPercent = -10.0)
                }
            }
        }

        listOf(0.8f, 1f, 0f).forEach { progress ->
            rule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(progress, 0f..1f))).assertIsDisplayed()
        }
    }
}
