package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsBrakeFailureThicknessDefaultsTest {
    @Test
    fun `Hypercar・LMP2・LMP2_ELMS・LMP3は25mm`() {
        assertEquals(
            LmuWindowsBrakeThicknessMeters(0.025f),
            lmuWindowsVehicleClassBrakeFailureThicknessDefault(LmuWindowsVehicleClassData.Hypercar),
        )
        assertEquals(
            LmuWindowsBrakeThicknessMeters(0.025f),
            lmuWindowsVehicleClassBrakeFailureThicknessDefault(LmuWindowsVehicleClassData.P2),
        )
        assertEquals(
            LmuWindowsBrakeThicknessMeters(0.025f),
            lmuWindowsVehicleClassBrakeFailureThicknessDefault(LmuWindowsVehicleClassData.P2Elms),
        )
        assertEquals(
            LmuWindowsBrakeThicknessMeters(0.025f),
            lmuWindowsVehicleClassBrakeFailureThicknessDefault(LmuWindowsVehicleClassData.P3),
        )
    }

    @Test
    fun `GTEとGT3は30mm`() {
        assertEquals(
            LmuWindowsBrakeThicknessMeters(0.030f),
            lmuWindowsVehicleClassBrakeFailureThicknessDefault(LmuWindowsVehicleClassData.Gte),
        )
        assertEquals(
            LmuWindowsBrakeThicknessMeters(0.030f),
            lmuWindowsVehicleClassBrakeFailureThicknessDefault(LmuWindowsVehicleClassData.Gt3),
        )
    }

    @Test
    fun `未知のクラスは25mm`() {
        assertEquals(
            LmuWindowsBrakeThicknessMeters(0.025f),
            lmuWindowsVehicleClassBrakeFailureThicknessDefault(LmuWindowsVehicleClassData.Unknown("X")),
        )
    }
}
