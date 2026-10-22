package kurou.kodriver.domain.model

import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutItemKeyTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerLmuWindowsReadoutItemKeys()
    }

    @Test
    fun `LMU keys retain legacy order and every key round trips`() {
        assertEquals(28, LMU_WINDOWS_READOUT_ITEM_KEYS.size)
        assertEquals(LmuWindowsReadoutItemKey.VehicleApproach.Root, LMU_WINDOWS_READOUT_ITEM_KEYS.first())
        assertEquals(LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled, LMU_WINDOWS_READOUT_ITEM_KEYS.last())
        assertEquals(
            LMU_WINDOWS_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>(),
            ReadoutItemKey.topLevelEntriesOf(Simulator.LmuWindows),
        )
        LMU_WINDOWS_READOUT_ITEM_KEYS.forEach { key -> assertEquals(key, ReadoutItemKey.fromValue(key.value)) }
    }

    @Test
    fun `LMU defaults cover all TopLevel keys and queueable keys`() {
        val topLevel = LMU_WINDOWS_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>()
        assertEquals(topLevel.toSet(), LMU_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT.keys)
        assertEquals(topLevel.toSet(), LMU_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT.keys)
        assertEquals(topLevel.filter { it.supportsQueue }.toSet(), LMU_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT.keys)
        assertEquals(Simulator.LmuWindows, lmuWindowsReadoutSpec.simulator)
        assertEquals(
            listOf(
                LmuWindowsReadoutItemKey.Flag.Root,
                LmuWindowsReadoutItemKey.VehicleApproach.Root,
                LmuWindowsReadoutItemKey.PitTiming.Root,
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                LmuWindowsReadoutItemKey.TyreTemperature.Root,
                LmuWindowsReadoutItemKey.TyreWear.Root,
                LmuWindowsReadoutItemKey.BrakeTemperature.Root,
                LmuWindowsReadoutItemKey.BrakeWear.Root,
                LmuWindowsReadoutItemKey.VehicleDamage.Root,
                LmuWindowsReadoutItemKey.MyBestLap.Root,
            ),
            lmuWindowsReadoutSpec.defaultAutoReadoutOrder,
        )
        assertEquals(LMU_WINDOWS_READOUT_ITEM_KEYS, ReadoutItemKeyRegistry.specOf(Simulator.LmuWindows).keys)
    }
}
