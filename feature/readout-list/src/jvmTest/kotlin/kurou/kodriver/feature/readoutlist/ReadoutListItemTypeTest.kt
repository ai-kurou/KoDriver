package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReadoutListItemTypeTest {
    @Test
    fun `LmuWindowsのアイテムはlmu_windowsシミュレータにbelongsToがtrueを返す`() {
        assertTrue(LmuWindowsReadoutListItemType.Flag.belongsTo(Simulator.LmuWindows))
    }

    @Test
    fun `LmuWindowsのアイテムはgt7_ps5シミュレータにbelongsToがfalseを返す`() {
        assertFalse(LmuWindowsReadoutListItemType.Flag.belongsTo(Simulator.Gt7Ps5))
    }

    @Test
    fun `Gt7Ps5のアイテムはgt7_ps5シミュレータにbelongsToがtrueを返す`() {
        assertTrue(Gt7Ps5ReadoutListItemType.MyBestLap.belongsTo(Simulator.Gt7Ps5))
    }

    @Test
    fun `AceWindowsのアイテムはace_windowsシミュレータにbelongsToがtrueを返す`() {
        assertTrue(AceWindowsReadoutListItemType.Flag.belongsTo(Simulator.AceWindows))
    }

    @Test
    fun `AceWindowsのアイテムはlmu_windowsシミュレータにbelongsToがfalseを返す`() {
        assertFalse(AceWindowsReadoutListItemType.Flag.belongsTo(Simulator.LmuWindows))
    }
}
