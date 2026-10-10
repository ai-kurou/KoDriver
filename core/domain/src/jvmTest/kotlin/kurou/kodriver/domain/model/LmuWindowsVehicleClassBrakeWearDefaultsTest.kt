package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleClassBrakeWearDefaultsTest {
    @Test
    fun `lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefaultは各クラスのデフォルト値を返す`() {
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_HYPERCAR_DEFAULT,
            lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(LmuWindowsVehicleClassData.Hypercar),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_P2_DEFAULT,
            lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(LmuWindowsVehicleClassData.P2),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_P2_ELMS_DEFAULT,
            lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(LmuWindowsVehicleClassData.P2Elms),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_P3_DEFAULT,
            lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(LmuWindowsVehicleClassData.P3),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_GTE_DEFAULT,
            lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(LmuWindowsVehicleClassData.Gte),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_GT3_DEFAULT,
            lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(LmuWindowsVehicleClassData.Gt3),
        )
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_UNKNOWN_DEFAULT,
            lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(
                LmuWindowsVehicleClassData.Unknown("Formula2026"),
            ),
        )
    }

    @Test
    fun `resolveLmuWindowsVehicleClassBrakeWearLowThresholdPercentはマップの値を返す`() {
        val thresholds: Map<LmuWindowsVehicleClassData, Int> = mapOf(LmuWindowsVehicleClassData.Gt3 to 650)

        assertEquals(
            650,
            resolveLmuWindowsVehicleClassBrakeWearLowThresholdPercent(
                thresholds,
                LmuWindowsVehicleClassData.Gt3,
            ),
        )
    }

    @Test
    fun `resolveLmuWindowsVehicleClassBrakeWearLowThresholdPercentはマップに無いクラスはデフォルト値を返す`() {
        assertEquals(
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_GT3_DEFAULT,
            resolveLmuWindowsVehicleClassBrakeWearLowThresholdPercent(
                emptyMap(),
                LmuWindowsVehicleClassData.Gt3,
            ),
        )
    }

    @Test
    fun `resolveLmuWindowsVehicleClassBrakeWearLowThresholdPercentはUnknownのraw値によらず代表キーを参照する`() {
        val thresholds: Map<LmuWindowsVehicleClassData, Int> =
            mapOf(LmuWindowsVehicleClassData.Unknown(LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY) to 650)

        assertEquals(
            650,
            resolveLmuWindowsVehicleClassBrakeWearLowThresholdPercent(
                thresholds,
                LmuWindowsVehicleClassData.Unknown("Formula2026"),
            ),
        )
    }
}
