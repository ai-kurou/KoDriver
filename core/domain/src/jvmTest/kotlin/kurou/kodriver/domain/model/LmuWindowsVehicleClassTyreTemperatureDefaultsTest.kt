package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleClassTyreTemperatureDefaultsTest {
    @Test
    fun `lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefaultは各クラスのデフォルト値を返す`() {
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_HYPERCAR_DEFAULT,
            lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.Hypercar),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_DEFAULT,
            lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.P2),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_ELMS_DEFAULT,
            lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.P2Elms),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P3_DEFAULT,
            lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.P3),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GTE_DEFAULT,
            lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.Gte),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GT3_DEFAULT,
            lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.Gt3),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_UNKNOWN_DEFAULT,
            lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(
                LmuWindowsVehicleClassData.Unknown("Formula2026"),
            ),
        )
    }

    @Test
    fun `lmuWindowsAllVehicleClassesは既知の6クラスとUnknownの代表インスタンスを1件含む`() {
        assertEquals(
            listOf(
                LmuWindowsVehicleClassData.Hypercar,
                LmuWindowsVehicleClassData.P2,
                LmuWindowsVehicleClassData.P2Elms,
                LmuWindowsVehicleClassData.P3,
                LmuWindowsVehicleClassData.Gte,
                LmuWindowsVehicleClassData.Gt3,
                LmuWindowsVehicleClassData.Unknown(LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY),
            ),
            lmuWindowsAllVehicleClasses,
        )
    }
}
