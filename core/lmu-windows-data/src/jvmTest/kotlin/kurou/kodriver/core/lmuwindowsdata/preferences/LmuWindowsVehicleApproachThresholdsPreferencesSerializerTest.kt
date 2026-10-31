package kurou.kodriver.core.lmuwindowsdata.preferences

import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleApproachThresholdsPreferencesSerializerTest {
    @Test
    fun `デフォルト値は縦方向5m・横方向5m・継続時間7秒`() {
        val expected =
            LmuWindowsVehicleApproachThresholdsPreferences(
                longitudinalThresholdMeters = LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT,
                lateralThresholdMeters = LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT,
                sustainedApproachDurationSeconds = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
            )
        assertEquals(expected, LmuWindowsVehicleApproachThresholdsPreferencesSerializer.defaultValue)
    }
}
