package kurou.kodriver.domain.lmuwindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleClassEnergySourceTest {
    @Test
    fun `lmuWindowsVehicleClassEnergySourceは各クラスのエネルギー源を返す`() {
        assertEquals(
            LmuWindowsEnergySource.VirtualEnergy,
            lmuWindowsVehicleClassEnergySource(LmuWindowsVehicleClassData.Hypercar),
        )
        assertEquals(
            LmuWindowsEnergySource.Fuel,
            lmuWindowsVehicleClassEnergySource(LmuWindowsVehicleClassData.P2),
        )
        assertEquals(
            LmuWindowsEnergySource.Fuel,
            lmuWindowsVehicleClassEnergySource(LmuWindowsVehicleClassData.P2Elms),
        )
        assertEquals(
            LmuWindowsEnergySource.Fuel,
            lmuWindowsVehicleClassEnergySource(LmuWindowsVehicleClassData.P3),
        )
        assertEquals(
            LmuWindowsEnergySource.Fuel,
            lmuWindowsVehicleClassEnergySource(LmuWindowsVehicleClassData.Gte),
        )
        assertEquals(
            LmuWindowsEnergySource.VirtualEnergy,
            lmuWindowsVehicleClassEnergySource(LmuWindowsVehicleClassData.Gt3),
        )
    }

    @Test
    fun `lmuWindowsVehicleClassEnergySourceは未知クラスのraw値によらずFuelを返す`() {
        listOf("Formula2026", "", "UNKNOWN").forEach { raw ->
            assertEquals(
                LmuWindowsEnergySource.Fuel,
                lmuWindowsVehicleClassEnergySource(LmuWindowsVehicleClassData.Unknown(raw)),
            )
        }
    }

    @Test
    fun `lmuWindowsVehicleClassEnergySourceは全車両クラスを期待どおりに判定する`() {
        val unknownRepresentative = LmuWindowsVehicleClassData.Unknown(LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY)
        val expectedEnergySources =
            mapOf(
                LmuWindowsVehicleClassData.Hypercar to LmuWindowsEnergySource.VirtualEnergy,
                LmuWindowsVehicleClassData.P2 to LmuWindowsEnergySource.Fuel,
                LmuWindowsVehicleClassData.P2Elms to LmuWindowsEnergySource.Fuel,
                LmuWindowsVehicleClassData.P3 to LmuWindowsEnergySource.Fuel,
                LmuWindowsVehicleClassData.Gte to LmuWindowsEnergySource.Fuel,
                LmuWindowsVehicleClassData.Gt3 to LmuWindowsEnergySource.VirtualEnergy,
                unknownRepresentative to LmuWindowsEnergySource.Fuel,
            )

        lmuWindowsAllVehicleClasses.forEach { vehicleClass ->
            assertEquals(
                expectedEnergySources.getValue(vehicleClass),
                lmuWindowsVehicleClassEnergySource(vehicleClass),
            )
        }
    }
}
