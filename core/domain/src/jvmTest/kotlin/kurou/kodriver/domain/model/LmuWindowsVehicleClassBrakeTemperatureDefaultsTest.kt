package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleClassBrakeTemperatureDefaultsTest {
    @Test
    fun `lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefaultは各クラスのデフォルト値を返す`() {
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_HYPERCAR_DEFAULT,
            lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.Hypercar),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_DEFAULT,
            lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.P2),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_ELMS_DEFAULT,
            lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.P2Elms),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P3_DEFAULT,
            lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.P3),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GTE_DEFAULT,
            lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.Gte),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GT3_DEFAULT,
            lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.Gt3),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_UNKNOWN_DEFAULT,
            lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(
                LmuWindowsVehicleClassData.Unknown("Formula2026"),
            ),
        )
    }

    @Test
    fun `resolveLmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusはマップの値を返す`() {
        val thresholds: Map<LmuWindowsVehicleClassData, Int> = mapOf(LmuWindowsVehicleClassData.Gt3 to 650)

        assertEquals(
            650,
            resolveLmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsius(
                thresholds,
                LmuWindowsVehicleClassData.Gt3,
            ),
        )
    }

    @Test
    fun `resolveLmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusはマップに無いクラスはデフォルト値を返す`() {
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GT3_DEFAULT,
            resolveLmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsius(
                emptyMap(),
                LmuWindowsVehicleClassData.Gt3,
            ),
        )
    }

    @Test
    fun `resolveLmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusはUnknownのraw値によらず代表キーを参照する`() {
        val thresholds: Map<LmuWindowsVehicleClassData, Int> =
            mapOf(LmuWindowsVehicleClassData.Unknown(LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY) to 650)

        assertEquals(
            650,
            resolveLmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsius(
                thresholds,
                LmuWindowsVehicleClassData.Unknown("Formula2026"),
            ),
        )
    }
}
