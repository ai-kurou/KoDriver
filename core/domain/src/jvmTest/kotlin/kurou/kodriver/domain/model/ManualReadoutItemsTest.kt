package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutItemsTest {
    @Test
    fun `項目名は詳細画面の日本語名と一致し未対応キーは空になる`() {
        assertEquals("バーチャルエナジー残量", manualReadoutItemName(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value))
        assertEquals("タイヤ摩耗", manualReadoutItemName(LmuWindowsReadoutItemKey.TyreWear.Root.value))
        assertEquals("", manualReadoutItemName("unknown"))
    }

    @Test
    fun `LMUは残量とタイヤ摩耗の順に選択でき未対応シミュレーターは空を返す`() {
        assertEquals(
            listOf(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                LmuWindowsReadoutItemKey.TyreWear.Root.value,
            ),
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
        assertEquals("タイヤ摩耗残り{percent}%", manualReadoutItemTextDefault(LmuWindowsReadoutItemKey.TyreWear.Root.value))
        assertEquals("", manualReadoutItemTextDefault("unknown"))
        assertEquals(emptyMap(), ManualReadoutSet("set-1").itemTexts)
    }
}
