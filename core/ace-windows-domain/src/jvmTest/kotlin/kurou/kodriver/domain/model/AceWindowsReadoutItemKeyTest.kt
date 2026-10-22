package kurou.kodriver.domain.model

import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsReadoutItemKeyTest {
    @BeforeTest
    fun registerReadoutKeys() {
        registerAceWindowsReadoutItemKeys()
    }

    @Test
    fun `ACE keys retain legacy order and every key round trips`() {
        assertEquals(21, ACE_WINDOWS_READOUT_ITEM_KEYS.size)
        assertEquals(AceWindowsReadoutItemKey.VehicleApproach.Root, ACE_WINDOWS_READOUT_ITEM_KEYS.first())
        assertEquals(AceWindowsReadoutItemKey.MyBestLap.DetailEnabled, ACE_WINDOWS_READOUT_ITEM_KEYS.last())
        assertEquals(
            ACE_WINDOWS_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>(),
            ReadoutItemKey.topLevelEntriesOf(Simulator.AceWindows),
        )
        ACE_WINDOWS_READOUT_ITEM_KEYS.forEach { key -> assertEquals(key, ReadoutItemKey.fromValue(key.value)) }
    }

    @Test
    fun `ACE defaults cover all TopLevel keys and queueable keys`() {
        val topLevel = ACE_WINDOWS_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>()
        assertEquals(topLevel.toSet(), ACE_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT.keys)
        assertEquals(topLevel.toSet(), ACE_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT.keys)
        assertEquals(topLevel.filter { it.supportsQueue }.toSet(), ACE_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT.keys)
        assertEquals(Simulator.AceWindows, aceWindowsReadoutSpec.simulator)
        assertEquals(
            listOf(
                AceWindowsReadoutItemKey.Flag.Root,
                AceWindowsReadoutItemKey.VehicleApproach.Root,
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
                AceWindowsReadoutItemKey.RemainingFuel.Root,
                AceWindowsReadoutItemKey.TyreTemperature.Root,
                AceWindowsReadoutItemKey.MyBestLap.Root,
            ),
            aceWindowsReadoutSpec.defaultAutoReadoutOrder,
        )
        assertEquals(ACE_WINDOWS_READOUT_ITEM_KEYS, ReadoutItemKeyRegistry.specOf(Simulator.AceWindows).keys)
    }
}
