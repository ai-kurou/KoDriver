package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsPitTimingReadoutTextTest {
    @Test
    fun `プレースホルダーがなければ文言を維持する`() {
        assertEquals("ピットイン", formatLmuWindowsPitTimingVirtualEnergyReadoutText("ピットイン", 1))
    }

    @Test
    fun `周回数の境界値を単純に置換する`() {
        listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE).forEach { laps ->
            assertEquals("残り${laps}周", formatLmuWindowsPitTimingVirtualEnergyReadoutText("残り{laps}周", laps))
        }
    }

    @Test
    fun `複数のプレースホルダーを置換して未知のトークンを維持する`() {
        assertEquals("2・2・{unknown}", formatLmuWindowsPitTimingVirtualEnergyReadoutText("{laps}・{laps}・{unknown}", 2))
    }
}
