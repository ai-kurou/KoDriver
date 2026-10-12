package kurou.kodriver.domain.model

import kurou.kodriver.domain.engine.DebugReadoutText
import kotlin.test.Test
import kotlin.test.assertEquals

class DebugReadoutRequestTest {
    @Test
    fun `各シミュレーターのTopLevelだけをentries順で返す`() {
        val expected =
            mapOf(
                Simulator.LmuWindows to
                    listOf(
                        LmuWindowsReadoutItemKey.VehicleApproach.Root,
                        LmuWindowsReadoutItemKey.Flag.Root,
                        LmuWindowsReadoutItemKey.VehicleDamage.Root,
                        LmuWindowsReadoutItemKey.TyreTemperature.Root,
                        LmuWindowsReadoutItemKey.PitTiming.Root,
                        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                        LmuWindowsReadoutItemKey.TyreWear.Root,
                        LmuWindowsReadoutItemKey.BrakeTemperature.Root,
                        LmuWindowsReadoutItemKey.BrakeWear.Root,
                        LmuWindowsReadoutItemKey.MyBestLap.Root,
                    ),
                Simulator.Gt7Ps5 to
                    listOf(
                        Gt7Ps5ReadoutItemKey.MyBestLap.Root,
                        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                        Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
                        Gt7Ps5ReadoutItemKey.TyreTemperature.Root,
                    ),
                Simulator.AceWindows to
                    listOf(
                        AceWindowsReadoutItemKey.VehicleApproach.Root,
                        AceWindowsReadoutItemKey.Flag.Root,
                        AceWindowsReadoutItemKey.RemainingFuel.Root,
                        AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
                        AceWindowsReadoutItemKey.TyreTemperature.Root,
                        AceWindowsReadoutItemKey.MyBestLap.Root,
                    ),
            )
        assertEquals(Simulator.entries.toSet(), expected.keys)
        Simulator.entries.forEach { simulator ->
            val actual = ReadoutItemKey.topLevelEntriesOf(simulator)
            assertEquals(expected.getValue(simulator), actual)
            assertEquals(actual, ReadoutItemKey.entries.filter { it in actual })
        }
    }

    @Test
    fun `デバッグ文言と置換後の文言は入力をそのまま保持する`() {
        val key = ReadoutItemKey.topLevelEntriesOf(Simulator.Gt7Ps5).first()
        val event = DebugReadoutText(key, " {laps} ")
        assertEquals(" {laps} ", event.narratedText)
        assertEquals(" {laps} ", event.resolvedText)
        assertEquals(DebugReadoutText(key, "変更"), event.withResolvedText("変更"))
        assertEquals("変更", event.withResolvedText("変更").narratedText)
    }
}
