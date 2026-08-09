package kurou.kodriver.feature.readoutlist

import kurou.kodriver.core.model.Simulator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReadoutListItemTypeTest {
    @Test
    fun `LmuWindowsのアイテムはlmu_windowsシミュレータにbelongsToがtrueを返す`() {
        assertTrue(ReadoutListItemType.LmuWindows.Flag.belongsTo(Simulator.LmuWindows))
    }

    @Test
    fun `LmuWindowsのアイテムはgt7_ps5シミュレータにbelongsToがfalseを返す`() {
        assertFalse(ReadoutListItemType.LmuWindows.Flag.belongsTo(Simulator.Gt7Ps5))
    }

    @Test
    fun `Gt7Ps5のアイテムはgt7_ps5シミュレータにbelongsToがtrueを返す`() {
        assertTrue(ReadoutListItemType.Gt7Ps5.MyBestLap.belongsTo(Simulator.Gt7Ps5))
    }

    @Test
    fun `AceWindowsのアイテムはace_windowsシミュレータにbelongsToがtrueを返す`() {
        assertTrue(ReadoutListItemType.AceWindows.Flag.belongsTo(Simulator.AceWindows))
    }

    @Test
    fun `AceWindowsのアイテムはlmu_windowsシミュレータにbelongsToがfalseを返す`() {
        assertFalse(ReadoutListItemType.AceWindows.Flag.belongsTo(Simulator.LmuWindows))
    }
}
