package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Straighten
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsItemIconTest {
    @Test
    fun `手動専用の前後車ギャップは距離アイコンを返す`() {
        assertEquals(Icons.Filled.Straighten, aceWindowsItemIcon(AceWindowsReadoutItemKey.CarGap.Root))
    }

    @Test
    fun `ブレーキ温度は温度計アイコンを返す`() {
        assertEquals(
            Icons.Filled.DeviceThermostat,
            aceWindowsItemIcon(AceWindowsReadoutItemKey.BrakeTemperature.Root),
        )
    }

    @Test
    fun `接近車両は車両アイコンを返す`() {
        assertEquals(Icons.Filled.DirectionsCar, aceWindowsItemIcon(AceWindowsReadoutItemKey.VehicleApproach.Root))
    }
}
