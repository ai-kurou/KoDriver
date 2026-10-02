package kurou.kodriver.data.preferences

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LmuWindowsVehicleApproachPreferencesTest {
    @Test
    fun `デフォルト値は skipFirstLap が true`() {
        assertEquals(true, LmuWindowsVehicleApproachPreferences().skipFirstLap)
    }

    @Test
    fun `左右の開始文言は既定値を返す`() {
        assertEquals("カーレフト", LmuWindowsVehicleApproachPreferences().startLeftReadoutText)
        assertEquals("カーライト", LmuWindowsVehicleApproachPreferences().startRightReadoutText)
    }

    @Test
    fun `左右の継続文言は既定値を返す`() {
        assertEquals("キープライト", LmuWindowsVehicleApproachPreferences().sustainedLeftReadoutText)
        assertEquals("キープレフト", LmuWindowsVehicleApproachPreferences().sustainedRightReadoutText)
    }

    @Test
    fun `copy で skipFirstLap を変更できる`() {
        val original = LmuWindowsVehicleApproachPreferences(skipFirstLap = false)
        val updated = original.copy(skipFirstLap = true)

        assertEquals(true, updated.skipFirstLap)
        assertEquals(false, original.skipFirstLap)
    }

    @Test
    fun `同じ値を持つインスタンスは等しい`() {
        assertEquals(
            LmuWindowsVehicleApproachPreferences(skipFirstLap = true),
            LmuWindowsVehicleApproachPreferences(skipFirstLap = true),
        )
    }

    @Test
    fun `異なる値を持つインスタンスは等しくない`() {
        assertNotEquals(
            LmuWindowsVehicleApproachPreferences(skipFirstLap = false),
            LmuWindowsVehicleApproachPreferences(skipFirstLap = true),
        )
    }
}
