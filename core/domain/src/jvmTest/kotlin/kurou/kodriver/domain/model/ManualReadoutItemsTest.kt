package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutItemsTest {
    @Test
    fun `LMUだけ仮想エネルギー残量を選択でき未知シミュレーターは空を返す`() {
        assertEquals(
            listOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value),
            manualReadoutItemKeys(Simulator.LmuWindows.id),
        )
        assertEquals(emptyList(), manualReadoutItemKeys(Simulator.AceWindows.id))
        assertEquals(emptyList(), manualReadoutItemKeys(Simulator.Gt7Ps5.id))
        assertEquals(emptyList(), manualReadoutItemKeys("unknown"))
    }

    @Test
    fun `残量項目の既定文言は従来と同じ発話になり未知項目は空になる`() {
        val template = manualReadoutItemTextDefault(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value)
        assertEquals("バーチャルエナジー残量は42パーセント", formatLmuWindowsRemainingVirtualEnergyReadoutText(template, 42))
        assertEquals("", manualReadoutItemTextDefault("unknown"))
        assertEquals(emptyMap(), ManualReadoutSet("set-1").itemTexts)
    }
}
