package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleClassTyreTemperaturePreferencesSerializerTest {
    @Test
    fun `デフォルト値は highThresholdCelsiusByVehicleClass が空Map`() {
        assertEquals(
            LmuWindowsVehicleClassTyreTemperaturePreferences(highThresholdCelsiusByVehicleClass = emptyMap()),
            LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer.defaultValue,
        )
    }
}
