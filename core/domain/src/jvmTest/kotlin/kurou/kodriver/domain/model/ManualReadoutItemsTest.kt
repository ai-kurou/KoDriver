package kurou.kodriver.domain.model

import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5RemainingFuelReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsRemainingVirtualEnergyReadoutText
import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutItemsTest {
    @Test
    fun `ACEの残り燃料は既存キーと手動専用文言と名前を持つ`() {
        val key = AceWindowsReadoutItemKey.RemainingFuel.Root.value
        assertEquals("ace_windows_auto_remaining_fuel", key)
        assertEquals("残り燃料", manualReadoutItemName(key))
        assertEquals("燃料は残り{percent}パーセント", manualReadoutItemTextDefault(key))
        assertEquals(42, AceWindowsManualReadoutItem(key, "", 42).value)
        assertEquals(null, AceWindowsManualReadoutItem(key, "", null).value)
        assertEquals(false, isLmuWindowsManualReadoutTemperatureItem(key))
    }

    @Test
    fun `ACEの燃料残り周回数は名前と手動専用文言を持ちキーは自動読み上げと共通`() {
        val key = AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value
        assertEquals("ace_windows_auto_remaining_fuel_laps", key)
        assertEquals("残り燃料の周回数", manualReadoutItemName(key))
        assertEquals("燃料は残り約{laps}周", manualReadoutItemTextDefault(key))
        assertEquals(5, AceWindowsManualReadoutItem(key, "", 5).value)
    }

    @Test
    fun `ACEの自己ベストラップは自己ベスト文言を使う`() {
        val key = AceWindowsReadoutItemKey.MyBestLap.Root.value
        assertEquals("自己ベストラップ", manualReadoutItemName(key))
        assertEquals("自己ベストラップ {laptime}", manualReadoutItemTextDefault(key))
    }

    @Test
    fun `LMUのエネルギー周回数は名前と既定文言と省略可能な周回数を持つ`() {
        val key = LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value
        assertEquals("エネルギーの残り周回数", manualReadoutItemName(key))
        assertEquals("エネルギーは残り約{laps}周", manualReadoutItemTextDefault(key))
        assertEquals(null, LmuWindowsManualReadoutItem(key, "", null).remainingLaps)
        assertEquals(false, isLmuWindowsManualReadoutTemperatureItem(key))
    }

    @Test
    fun `LMUのタイヤ摩耗周回数は名前と既定文言を持つ`() {
        val key = LmuWindowsReadoutItemKey.PitTiming.TyreWear.value
        assertEquals("タイヤ摩耗の残り周回数", manualReadoutItemName(key))
        assertEquals("タイヤは残り約{laps}周", manualReadoutItemTextDefault(key))
        assertEquals(false, isLmuWindowsManualReadoutTemperatureItem(key))
    }

    @Test
    fun `LMUの自己ベストは手動専用文言と名前と省略可能なラップタイムを持つ`() {
        val key = LmuWindowsReadoutItemKey.MyBestLap.Root.value
        assertEquals("自己ベストラップ", manualReadoutItemName(key))
        assertEquals("自己ベストラップ {laptime}", manualReadoutItemTextDefault(key))
        assertEquals(null, LmuWindowsManualReadoutItem(key, "", null).lapTimeMs)
        assertEquals(false, isLmuWindowsManualReadoutTemperatureItem(key))
    }

    @Test
    fun `GT7のタイヤ温度は名前と既定文言を持つ`() {
        val key = Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value
        assertEquals("タイヤ温度", manualReadoutItemName(key))
        assertEquals("タイヤ温度{celsius}℃", manualReadoutItemTextDefault(key))
        assertEquals(emptySet(), manualReadoutItemKeys(Simulator.LmuWindows.id).toSet().intersect(setOf(key)))
    }

    @Test
    fun `GT7の自己ベストラップは名前と既定文言を持ちLMUと衝突しない`() {
        val key = Gt7Ps5ReadoutItemKey.MyBestLap.Root.value
        assertEquals("自己ベストラップ", manualReadoutItemName(key))
        assertEquals("自己ベストラップ {laptime}", manualReadoutItemTextDefault(key))
        assertEquals(false, key == LmuWindowsReadoutItemKey.MyBestLap.Root.value)
        assertEquals(emptySet(), manualReadoutItemKeys(Simulator.LmuWindows.id).toSet().intersect(setOf(key)))
    }

    @Test
    fun `GT7の周回数は名前と既定文言を持ちLMUと衝突しない`() {
        val key = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value
        assertEquals("残り燃料の周回数", manualReadoutItemName(key))
        assertEquals("燃料は残り約{laps}周", manualReadoutItemTextDefault(key))
        assertEquals(emptySet(), manualReadoutItemKeys(Simulator.LmuWindows.id).toSet().intersect(setOf(key)))
        assertEquals(3, Gt7Ps5ManualReadoutItem(key, "", 3).value)
    }

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
        assertEquals("バーチャルエネルギー残量", manualReadoutItemName(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value))
        assertEquals("タイヤ摩耗", manualReadoutItemName(LmuWindowsReadoutItemKey.TyreWear.Root.value))
        assertEquals("ブレーキ残量", manualReadoutItemName(LmuWindowsReadoutItemKey.BrakeWear.Root.value))
        assertEquals("ブレーキ温度", manualReadoutItemName(LmuWindowsReadoutItemKey.BrakeTemperature.Root.value))
        assertEquals("タイヤ温度", manualReadoutItemName(LmuWindowsReadoutItemKey.TyreTemperature.Root.value))
        assertEquals("", manualReadoutItemName("unknown"))
    }

    @Test
    fun `LMUは残量とタイヤ摩耗とブレーキ残量とブレーキ温度とタイヤ温度と自己ベストとエネルギー周回数とタイヤ摩耗周回数の順に選択でき未対応シミュレーターは空を返す`() {
        assertEquals(
            listOf(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                LmuWindowsReadoutItemKey.TyreWear.Root.value,
                LmuWindowsReadoutItemKey.BrakeWear.Root.value,
                LmuWindowsReadoutItemKey.BrakeTemperature.Root.value,
                LmuWindowsReadoutItemKey.TyreTemperature.Root.value,
                LmuWindowsReadoutItemKey.MyBestLap.Root.value,
                LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value,
                LmuWindowsReadoutItemKey.PitTiming.TyreWear.value,
            ),
            manualReadoutItemKeys(Simulator.LmuWindows.id),
        )
        assertEquals(
            listOf(
                AceWindowsReadoutItemKey.RemainingFuel.Root.value,
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value,
                AceWindowsReadoutItemKey.MyBestLap.Root.value,
            ),
            manualReadoutItemKeys(Simulator.AceWindows.id),
        )
        assertEquals(
            listOf(
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value,
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value,
                Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value,
                Gt7Ps5ReadoutItemKey.MyBestLap.Root.value,
            ),
            manualReadoutItemKeys(Simulator.Gt7Ps5.id),
        )
        assertEquals(emptyList(), manualReadoutItemKeys("unknown"))
    }

    @Test
    fun `残量項目の既定文言は従来と同じ発話になり未知項目は空になる`() {
        val template = manualReadoutItemTextDefault(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value)
        assertEquals("バーチャルエネルギー残量は42パーセント", formatLmuWindowsRemainingVirtualEnergyReadoutText(template, 42))
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
