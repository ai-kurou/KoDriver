package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutItemsTest {
    @Test
    fun `GT7の残り燃料は既定文言と名前を持ちLMUのキーと衝突しない`() {
        val key = Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value
        assertEquals("残り燃料", manualReadoutItemName(key))
        assertEquals(GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT, manualReadoutItemTextDefault(key))
        assertEquals("燃料は残り42パーセント", formatGt7Ps5RemainingFuelReadoutText(manualReadoutItemTextDefault(key), 42))
        assertEquals(emptySet(), manualReadoutItemKeys(Simulator.LmuWindows.id).toSet().intersect(setOf(key)))
        assertEquals(false, isLmuWindowsManualReadoutTemperatureItem(key))
    }

    @Test
    fun `温度項目はブレーキとタイヤのみで割合項目や未知項目と区別する`() {
        val cases =
            listOf(
                LmuWindowsReadoutItemKey.BrakeTemperature.Root.value to true,
                LmuWindowsReadoutItemKey.TyreTemperature.Root.value to true,
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value to false,
                LmuWindowsReadoutItemKey.TyreWear.Root.value to false,
                LmuWindowsReadoutItemKey.BrakeWear.Root.value to false,
                "unknown" to false,
            )
        cases.forEach { (key, expected) ->
            assertEquals(expected, isLmuWindowsManualReadoutTemperatureItem(key))
        }
    }

    @Test
    fun `項目名は詳細画面の日本語名と一致し未対応キーは空になる`() {
        assertEquals("バーチャルエナジー残量", manualReadoutItemName(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value))
        assertEquals("タイヤ摩耗", manualReadoutItemName(LmuWindowsReadoutItemKey.TyreWear.Root.value))
        assertEquals("ブレーキ残量", manualReadoutItemName(LmuWindowsReadoutItemKey.BrakeWear.Root.value))
        assertEquals("ブレーキ温度", manualReadoutItemName(LmuWindowsReadoutItemKey.BrakeTemperature.Root.value))
        assertEquals("タイヤ温度", manualReadoutItemName(LmuWindowsReadoutItemKey.TyreTemperature.Root.value))
        assertEquals("", manualReadoutItemName("unknown"))
    }

    @Test
    fun `LMUは残量とタイヤ摩耗とブレーキ残量とブレーキ温度とタイヤ温度の順に選択でき未対応シミュレーターは空を返す`() {
        assertEquals(
            listOf(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                LmuWindowsReadoutItemKey.TyreWear.Root.value,
                LmuWindowsReadoutItemKey.BrakeWear.Root.value,
                LmuWindowsReadoutItemKey.BrakeTemperature.Root.value,
                LmuWindowsReadoutItemKey.TyreTemperature.Root.value,
            ),
            manualReadoutItemKeys(Simulator.LmuWindows.id),
        )
        assertEquals(emptyList(), manualReadoutItemKeys(Simulator.AceWindows.id))
        assertEquals(listOf(Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value), manualReadoutItemKeys(Simulator.Gt7Ps5.id))
        assertEquals(emptyList(), manualReadoutItemKeys("unknown"))
    }

    @Test
    fun `残量項目の既定文言は従来と同じ発話になり未知項目は空になる`() {
        val template = manualReadoutItemTextDefault(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value)
        assertEquals("バーチャルエナジー残量は42パーセント", formatLmuWindowsRemainingVirtualEnergyReadoutText(template, 42))
        assertEquals("タイヤ摩耗残り{percent}%", manualReadoutItemTextDefault(LmuWindowsReadoutItemKey.TyreWear.Root.value))
        assertEquals("ブレーキ残量{percent}%", manualReadoutItemTextDefault(LmuWindowsReadoutItemKey.BrakeWear.Root.value))
        assertEquals(
            "ブレーキ温度{celsius}℃",
            manualReadoutItemTextDefault(LmuWindowsReadoutItemKey.BrakeTemperature.Root.value),
        )
        assertEquals(
            "タイヤ温度{celsius}℃",
            manualReadoutItemTextDefault(LmuWindowsReadoutItemKey.TyreTemperature.Root.value),
        )
        assertEquals("", manualReadoutItemTextDefault("unknown"))
        assertEquals(emptyMap(), ManualReadoutSet("set-1").itemTexts)
    }
}
