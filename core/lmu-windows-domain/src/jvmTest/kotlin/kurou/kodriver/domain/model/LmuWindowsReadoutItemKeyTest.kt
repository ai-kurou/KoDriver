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
        assertEquals(34, LMU_WINDOWS_READOUT_ITEM_KEYS.size)
        val fuel = LmuWindowsReadoutItemKey.PitTiming.Fuel
        assertEquals("lmu_windows_auto_pit_timing_fuel", fuel.value)
        assertEquals(
            false,
            ReadoutItemKey.topLevelEntriesOf(Simulator.LmuWindows).map { it.value }.contains(fuel.value),
        )
        val laps = LmuWindowsReadoutItemKey.RemainingFuel.Laps
        assertEquals("lmu_windows_auto_remaining_fuel_laps", laps.value)
        val topLevelValues = ReadoutItemKey.topLevelEntriesOf(Simulator.LmuWindows).map { it.value }
        assertEquals(false, topLevelValues.contains(laps.value))
        assertEquals("lmu_windows_auto_class_car_gap_ahead", LmuWindowsReadoutItemKey.ClassCarGap.Ahead.value)
        assertEquals("lmu_windows_auto_class_car_gap_behind", LmuWindowsReadoutItemKey.ClassCarGap.Behind.value)
        assertEquals(false, topLevelValues.contains(LmuWindowsReadoutItemKey.ClassCarGap.Ahead.value))
        assertEquals(false, topLevelValues.contains(LmuWindowsReadoutItemKey.ClassCarGap.Behind.value))
        assertEquals(LmuWindowsReadoutItemKey.VehicleApproach.Root, LMU_WINDOWS_READOUT_ITEM_KEYS.first())
        assertEquals(
            listOf(
                LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled,
                LmuWindowsReadoutItemKey.ClassCarGap.Ahead,
                LmuWindowsReadoutItemKey.ClassCarGap.Behind,
            ),
            LMU_WINDOWS_READOUT_ITEM_KEYS.takeLast(3),
        )
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
                LmuWindowsReadoutItemKey.RemainingFuel.Root,
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
