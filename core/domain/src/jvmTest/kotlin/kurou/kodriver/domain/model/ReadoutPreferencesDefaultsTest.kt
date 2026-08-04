package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutPreferencesDefaultsTest {
    @Test
    fun `保存済みの値があればそれを返す`() {
        val enabledStates: Map<ReadoutItemKey, Boolean> = mapOf(ReadoutItemKey.LmuWindows.MyBestLap.Root to true)

        assertEquals(true, enabledStates.readoutEnabled(ReadoutItemKey.LmuWindows.MyBestLap.Root))
    }

    @Test
    fun `未保存のTopLevelキーはREADOUT_ENABLED_STATE_DEFAULTの値を返す`() {
        val enabledStates = emptyMap<ReadoutItemKey, Boolean>()

        assertEquals(false, enabledStates.readoutEnabled(ReadoutItemKey.LmuWindows.VehicleDamage.Root))
        assertEquals(true, enabledStates.readoutEnabled(ReadoutItemKey.LmuWindows.Flag.Root))
    }

    @Test
    fun `未保存のサブ項目キーはデフォルトtrueを返す`() {
        val enabledStates = emptyMap<ReadoutItemKey, Boolean>()

        assertEquals(true, enabledStates.readoutEnabled(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat))
        assertEquals(true, enabledStates.readoutEnabled(ReadoutItemKey.LmuWindows.Flag.BlueFlag))
    }
}
