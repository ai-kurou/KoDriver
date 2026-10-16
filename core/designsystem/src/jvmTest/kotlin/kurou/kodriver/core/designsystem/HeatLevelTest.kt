package kurou.kodriver.core.designsystem

import kotlin.test.Test
import kotlin.test.assertEquals

class HeatLevelTest {
    @Test
    fun `全ヒートレベルはライトとダークの対応色を返す`() {
        listOf(LightExtendedColorScheme, DarkExtendedColorScheme).forEach { colors ->
            assertEquals(colors.heatCool, heatColor(HeatLevel.COOL, colors))
            assertEquals(colors.heatOk, heatColor(HeatLevel.OK, colors))
            assertEquals(colors.heatWarm, heatColor(HeatLevel.WARM, colors))
            assertEquals(colors.heatHot, heatColor(HeatLevel.HOT, colors))
        }
    }
}
