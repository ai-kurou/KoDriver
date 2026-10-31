package kurou.kodriver.data.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class SimulatorPreferencesSerializerTest {
    @Test
    fun `デフォルト値はSimulatorPreferencesの既定値と一致する`() {
        assertEquals(SimulatorPreferences(), SimulatorPreferencesSerializer.defaultValue)
    }
}
