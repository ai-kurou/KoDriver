package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Straighten
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsItemIconTest {
    @Test
    fun `手動専用のクラス内ギャップは距離アイコンを返す`() {
        assertEquals(Icons.Filled.Straighten, lmuWindowsItemIcon(LmuWindowsReadoutItemKey.ClassCarGap.Root))
    }

    @Test
    fun `接近車両は車両アイコンを返す`() {
        assertEquals(Icons.Filled.DirectionsCar, lmuWindowsItemIcon(LmuWindowsReadoutItemKey.VehicleApproach.Root))
    }

    @Test
    fun `残バーチャルエネルギーは稲妻アイコンを返す`() {
        assertEquals(Icons.Filled.Bolt, lmuWindowsItemIcon(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root))
    }

    @Test
    fun `残燃料は給油所アイコンを返す`() {
        assertEquals(Icons.Filled.LocalGasStation, lmuWindowsItemIcon(LmuWindowsReadoutItemKey.RemainingFuel.Root))
    }

    @Test
    fun `ブレーキ摩耗はディスクアイコンを返す`() {
        assertEquals(Icons.Filled.Album, lmuWindowsItemIcon(LmuWindowsReadoutItemKey.BrakeWear.Root))
    }
}
