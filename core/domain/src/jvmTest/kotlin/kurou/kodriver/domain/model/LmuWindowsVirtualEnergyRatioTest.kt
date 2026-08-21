package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsVirtualEnergyRatioTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(0.8, LmuWindowsVirtualEnergyRatio(0.8).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(LmuWindowsVirtualEnergyRatio(0.2) < LmuWindowsVirtualEnergyRatio(0.5))
        assertTrue(LmuWindowsVirtualEnergyRatio(0.5) > LmuWindowsVirtualEnergyRatio(0.2))
        assertEquals(0, LmuWindowsVirtualEnergyRatio(0.2).compareTo(LmuWindowsVirtualEnergyRatio(0.2)))
    }
}
