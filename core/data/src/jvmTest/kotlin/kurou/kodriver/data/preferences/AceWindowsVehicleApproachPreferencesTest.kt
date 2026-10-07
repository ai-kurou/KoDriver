package kurou.kodriver.data.preferences

import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsVehicleApproachPreferencesTest {
    @Test
    fun `デフォルト値はドメイン定数と一致する`() {
        val preferences = AceWindowsVehicleApproachPreferences()
        assertEquals(ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT, preferences.readoutText)
        assertEquals(ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT, preferences.thresholdMeters)
        assertEquals(emptyMap(), preferences.enabledStates)
    }
}
