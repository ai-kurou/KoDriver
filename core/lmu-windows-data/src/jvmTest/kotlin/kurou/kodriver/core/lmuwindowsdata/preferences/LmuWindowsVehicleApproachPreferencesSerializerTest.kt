package kurou.kodriver.core.lmuwindowsdata.preferences

import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_SKIP_FIRST_LAP_DEFAULT
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleApproachPreferencesSerializerTest {
    @Test
    fun `デフォルト値は初期設定を返す`() {
        assertEquals(
            LmuWindowsVehicleApproachPreferences(
                skipFirstLap = LMU_WINDOWS_VEHICLE_APPROACH_SKIP_FIRST_LAP_DEFAULT,
            ),
            LmuWindowsVehicleApproachPreferencesSerializer.defaultValue,
        )
    }
}
