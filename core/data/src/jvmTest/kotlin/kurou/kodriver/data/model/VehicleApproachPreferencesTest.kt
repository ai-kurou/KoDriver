package kurou.kodriver.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class VehicleApproachPreferencesTest {

    @Test
    fun `デフォルト値は skipFirstLap が true`() {
        assertEquals(true, VehicleApproachPreferences().skipFirstLap)
    }

    @Test
    fun `デフォルト値は startReadoutType が car_left_right`() {
        assertEquals("car_left_right", VehicleApproachPreferences().startReadoutType)
    }

    @Test
    fun `copy で skipFirstLap を変更できる`() {
        val original = VehicleApproachPreferences(skipFirstLap = false)
        val updated = original.copy(skipFirstLap = true)

        assertEquals(true, updated.skipFirstLap)
        assertEquals(false, original.skipFirstLap)
    }

    @Test
    fun `同じ値を持つインスタンスは等しい`() {
        assertEquals(VehicleApproachPreferences(skipFirstLap = true), VehicleApproachPreferences(skipFirstLap = true))
    }

    @Test
    fun `異なる値を持つインスタンスは等しくない`() {
        assertNotEquals(
            VehicleApproachPreferences(skipFirstLap = false),
            VehicleApproachPreferences(skipFirstLap = true),
        )
    }
}
