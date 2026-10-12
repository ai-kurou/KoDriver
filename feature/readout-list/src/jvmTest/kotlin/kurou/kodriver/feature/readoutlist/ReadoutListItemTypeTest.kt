package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReadoutListItemTypeTest {
    @Test
    fun `LmuWindowsのアイテムはlmu_windowsシミュレーターにbelongsToがtrueを返す`() {
        assertTrue(LmuWindowsReadoutListItemType.Flag.belongsTo(Simulator.LmuWindows))
    }

    @Test
    fun `LmuWindowsのアイテムはgt7_ps5シミュレーターにbelongsToがfalseを返す`() {
        assertFalse(LmuWindowsReadoutListItemType.Flag.belongsTo(Simulator.Gt7Ps5))
    }

    @Test
    fun `Gt7Ps5のアイテムはgt7_ps5シミュレーターにbelongsToがtrueを返す`() {
        assertTrue(Gt7Ps5ReadoutListItemType.MyBestLap.belongsTo(Simulator.Gt7Ps5))
    }

    @Test
    fun `AceWindowsのアイテムはace_windowsシミュレーターにbelongsToがtrueを返す`() {
        assertTrue(AceWindowsReadoutListItemType.Flag.belongsTo(Simulator.AceWindows))
    }

    @Test
    fun `AceWindowsのアイテムはlmu_windowsシミュレーターにbelongsToがfalseを返す`() {
        assertFalse(AceWindowsReadoutListItemType.Flag.belongsTo(Simulator.LmuWindows))
    }
}
