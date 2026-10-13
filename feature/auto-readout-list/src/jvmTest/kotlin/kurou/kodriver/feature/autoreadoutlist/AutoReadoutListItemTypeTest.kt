package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutoReadoutListItemTypeTest {
    @Test
    fun `LmuWindowsのアイテムはlmu_windowsシミュレーターにbelongsToがtrueを返す`() {
        assertTrue(LmuWindowsAutoReadoutListItemType.Flag.belongsTo(Simulator.LmuWindows))
    }

    @Test
    fun `LmuWindowsのアイテムはgt7_ps5シミュレーターにbelongsToがfalseを返す`() {
        assertFalse(LmuWindowsAutoReadoutListItemType.Flag.belongsTo(Simulator.Gt7Ps5))
    }

    @Test
    fun `Gt7Ps5のアイテムはgt7_ps5シミュレーターにbelongsToがtrueを返す`() {
        assertTrue(Gt7Ps5AutoReadoutListItemType.MyBestLap.belongsTo(Simulator.Gt7Ps5))
    }

    @Test
    fun `AceWindowsのアイテムはace_windowsシミュレーターにbelongsToがtrueを返す`() {
        assertTrue(AceWindowsAutoReadoutListItemType.Flag.belongsTo(Simulator.AceWindows))
    }

    @Test
    fun `AceWindowsのアイテムはlmu_windowsシミュレーターにbelongsToがfalseを返す`() {
        assertFalse(AceWindowsAutoReadoutListItemType.Flag.belongsTo(Simulator.LmuWindows))
    }
}
