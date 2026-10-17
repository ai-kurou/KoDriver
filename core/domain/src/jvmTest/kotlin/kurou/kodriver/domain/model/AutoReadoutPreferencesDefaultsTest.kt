package kurou.kodriver.domain.model

import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_AUTO_READOUT_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_QUEUE_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_READOUT_START_SOUND_ENABLED_STATE_DEFAULT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AutoReadoutPreferencesDefaultsTest {
    @Test
    fun `LMUのデフォルト定数にはLMUキーのみが含まれる`() {
        val defaults =
            listOf(
                LMU_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT,
                LMU_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT,
                LMU_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT,
            )

        defaults.forEach { enabledStates ->
            assertTrue(enabledStates.isNotEmpty())
            assertTrue(enabledStates.keys.all { it is LmuWindowsReadoutItemKey })
        }
    }

    @Test
    fun `GT7のデフォルト定数にはGT7キーのみが含まれる`() {
        val defaults =
            listOf(
                GT7_PS5_AUTO_READOUT_ENABLED_STATE_DEFAULT,
                GT7_PS5_READOUT_START_SOUND_ENABLED_STATE_DEFAULT,
                GT7_PS5_QUEUE_ENABLED_STATE_DEFAULT,
            )

        defaults.forEach { enabledStates ->
            assertTrue(enabledStates.isNotEmpty())
            assertTrue(enabledStates.keys.all { it is Gt7Ps5ReadoutItemKey })
        }
    }

    @Test
    fun `ACEのデフォルト定数にはACEキーのみが含まれる`() {
        val defaults =
            listOf(
                ACE_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT,
                ACE_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT,
                ACE_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT,
            )

        defaults.forEach { enabledStates ->
            assertTrue(enabledStates.isNotEmpty())
            assertTrue(enabledStates.keys.all { it is AceWindowsReadoutItemKey })
        }
    }

    @Test
    fun `保存済みの値があればそれを返す`() {
        val enabledStates: Map<ReadoutItemKey, Boolean> = mapOf(LmuWindowsReadoutItemKey.MyBestLap.Root to true)

        assertEquals(true, enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.MyBestLap.Root))
    }

    @Test
    fun `未保存のTopLevelキーはREADOUT_ENABLED_STATE_DEFAULTの値を返す`() {
        val enabledStates = emptyMap<ReadoutItemKey, Boolean>()

        assertEquals(false, enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.VehicleDamage.Root))
        assertEquals(true, enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.Flag.Root))
    }

    @Test
    fun `未保存のサブ項目キーはデフォルトtrueを返す`() {
        val enabledStates = emptyMap<ReadoutItemKey, Boolean>()

        assertEquals(true, enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.VehicleDamage.Overheat))
        assertEquals(true, enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.Flag.BlueFlag))
    }

    @Test
    fun `全てのTopLevelキーが所属シミュレーターのREADOUT_ENABLED_STATE_DEFAULTに列挙されている`() {
        val missing =
            ReadoutItemKey.entries
                .filterIsInstance<ReadoutItemKey.TopLevel>()
                .filterNot { it in READOUT_ENABLED_STATE_DEFAULT[it.simulator()].orEmpty() }

        assertEquals(emptyList(), missing)
    }

    @Test
    fun `READOUT_ENABLED_STATE_DEFAULTに別シミュレーターのキーが混入していない`() {
        val misplaced =
            READOUT_ENABLED_STATE_DEFAULT.flatMap { (simulator, enabledStates) ->
                enabledStates.keys.filter { it.simulator() != simulator }
            }

        assertEquals(emptyList(), misplaced)
    }

    @Test
    fun `supportsQueueがtrueのTopLevelキーがQUEUE_ENABLED_STATE_DEFAULTに列挙されている`() {
        val missing =
            ReadoutItemKey.entries
                .filterIsInstance<ReadoutItemKey.TopLevel>()
                .filter { it.supportsQueue } - QUEUE_ENABLED_STATE_DEFAULT.keys

        assertEquals(emptyList(), missing)
    }

    @Test
    fun `全てのTopLevelキーがREADOUT_START_SOUND_ENABLED_STATE_DEFAULTに列挙されている`() {
        val missing =
            ReadoutItemKey.entries.filterIsInstance<ReadoutItemKey.TopLevel>() -
                READOUT_START_SOUND_ENABLED_STATE_DEFAULT.keys

        assertEquals(emptyList(), missing)
    }
}

private fun ReadoutItemKey.simulator(): Simulator =
    when (this) {
        is LmuWindowsReadoutItemKey -> Simulator.LmuWindows
        is Gt7Ps5ReadoutItemKey -> Simulator.Gt7Ps5
        is AceWindowsReadoutItemKey -> Simulator.AceWindows
    }
