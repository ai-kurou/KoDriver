package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.PitTimingSource
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("TooManyFunctions")
class SpeechEventTest {
    @Test
    fun `LMU車両接近系のnarratedTextは既定文言を返す`() {
        assertEquals("カーレフト", SpeechEvent.CarLeft.narratedText)
        assertEquals("カーライト", SpeechEvent.CarRight.narratedText)
        assertEquals("キープレフト", SpeechEvent.CarRightSustained.narratedText)
        assertEquals("キープライト", SpeechEvent.CarLeftSustained.narratedText)
    }

    @Test
    fun `LMUフラッグ系のnarratedTextは既定文言を返す`() {
        assertEquals("ブルーフラッグ", SpeechEvent.BlueFlag.narratedText)
        assertEquals("イエローフラッグ", SpeechEvent.YellowFlag.narratedText)
        assertEquals("フルコースイエロー", SpeechEvent.FullCourseYellow.narratedText)
        assertEquals("レッドフラッグ", SpeechEvent.RedFlag.narratedText)
    }

    @Test
    fun `LMU車両故障系のnarratedTextはChipと同じ文言を返す`() {
        assertEquals("GP2 GP2… ahhh!!!", SpeechEvent.Overheating.narratedText)
        assertEquals("オーバーヒート", SpeechEvent.OverheatingStandard.narratedText)
        assertEquals("部品脱落", SpeechEvent.PartDetached.narratedText)
        assertEquals("タイヤ脱落", SpeechEvent.TyreDetached.narratedText)
    }

    @Test
    fun `LMUタイヤ・エナジー系のnarratedTextはChipと同じ文言を返す`() {
        assertEquals("タイヤ過熱 100度", SpeechEvent.TyreOverheat(100).narratedText)
        assertEquals("タイヤ低温 60度", SpeechEvent.TyreCold(60).narratedText)
        assertEquals("タイヤ残存率50%以下", SpeechEvent.TyreWearWarning(50).narratedText)
        assertEquals("バーチャルエナジー残量50%以下", SpeechEvent.RemainingVirtualEnergyWarning(50).narratedText)
    }

    @Test
    fun `タイヤ温度警告は温度と解決済み文言を保持し既定文言とキーを維持する`() {
        val overheat = SpeechEvent.TyreOverheat(100)
        val cold = SpeechEvent.TyreCold(60)
        assertEquals(null, overheat.resolvedText)
        assertEquals(null, cold.resolvedText)
        assertEquals(100, overheat.celsius)
        assertEquals(60, cold.celsius)
        assertEquals(ReadoutItemKey.LmuWindows.TyreTemperature.Root, overheat.readoutItemKey)
        assertEquals(ReadoutItemKey.LmuWindows.TyreTemperature.Root, cold.readoutItemKey)
        assertEquals("過熱100℃", overheat.copy(resolvedText = "過熱100℃").resolvedText)
        assertEquals("低温60℃", cold.copy(resolvedText = "低温60℃").resolvedText)
        assertEquals("タイヤ過熱 100度", overheat.copy(resolvedText = "過熱100℃").narratedText)
        assertEquals("タイヤ低温 60度", cold.copy(resolvedText = "低温60℃").narratedText)
    }

    @Test
    fun `自己ベストラップ系のnarratedTextはChipと同じ文言を返す`() {
        assertEquals("自己ベストラップ更新", SpeechEvent.LmuWindowsMyBestLapFormal.narratedText)
        assertEquals("ベストラップ", SpeechEvent.LmuWindowsMyBestLapCasual.narratedText)
        assertEquals("自己ベストラップ更新", SpeechEvent.Gt7Ps5MyBestLapFormal.narratedText)
        assertEquals("ベストラップ", SpeechEvent.Gt7Ps5MyBestLapCasual.narratedText)
        assertEquals("自己ベストラップ更新", SpeechEvent.AceWindowsMyBestLapFormal.narratedText)
        assertEquals("ベストラップ", SpeechEvent.AceWindowsMyBestLapCasual.narratedText)
    }

    @Test
    fun `RemainingFuelLapsWarningはlapsが1以上のとき残り周回数の文言を返す`() {
        assertEquals("燃料は残り約3周", SpeechEvent.RemainingFuelLapsWarning(laps = 3).narratedText)
    }

    @Test
    fun `RemainingFuelLapsWarningはlapsが0以下のとき燃料切れの文言を返す`() {
        assertEquals("燃料がありません", SpeechEvent.RemainingFuelLapsWarning(laps = 0).narratedText)
        assertEquals("燃料がありません", SpeechEvent.RemainingFuelLapsWarning(laps = -1).narratedText)
    }

    @Test
    fun `Gt7Ps5燃料残量はRootキーと既定文言を持ち解決文言をcopyで保持する`() {
        val event = SpeechEvent.Gt7Ps5RemainingFuelWarning(29)
        val resolved = event.copy(resolvedText = "残り29%")

        assertEquals(ReadoutItemKey.Gt7Ps5.RemainingFuel.Root, event.readoutItemKey)
        assertEquals("燃料は残り29パーセント", event.narratedText)
        assertEquals("燃料は残り29パーセント", resolved.narratedText)
        assertEquals("残り29%", resolved.resolvedText)
    }

    @Test
    fun `Gt7Ps5の燃料残量は整数を展開しタイヤは警告文言を返す`() {
        assertEquals("燃料は残り30パーセント", SpeechEvent.Gt7Ps5RemainingFuelWarning(30).narratedText)
        assertEquals("タイヤ過熱 107度", SpeechEvent.Gt7Ps5TyreOverheat(107).narratedText)
    }

    @Test
    fun `バーチャルエナジーの1以上の周回数は通常既定文言を置換する`() {
        listOf(1, 3).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT.replace("{laps}", laps.toString()),
                SpeechEvent.PitTimingWarning(laps, PitTimingSource.VirtualEnergy).narratedText,
            )
        }
    }

    @Test
    fun `バーチャルエナジーの0以下の周回数は切迫既定文言を返す`() {
        listOf(0, -1).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT,
                SpeechEvent.PitTimingWarning(laps, PitTimingSource.VirtualEnergy).narratedText,
            )
        }
    }

    @Test
    fun `タイヤ摩耗の1以上の周回数は通常既定文言を置換する`() {
        listOf(1, 3).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT.replace("{laps}", laps.toString()),
                SpeechEvent.PitTimingWarning(laps, PitTimingSource.TyreWear).narratedText,
            )
        }
    }

    @Test
    fun `タイヤ摩耗の0以下の周回数は切迫既定文言を返す`() {
        listOf(0, -1).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT,
                SpeechEvent.PitTimingWarning(laps, PitTimingSource.TyreWear).narratedText,
            )
        }
    }

    @Test
    fun `AceWindowsRemainingFuelLapsWarningはlapsが1以上のとき残り周回数の文言を返す`() {
        assertEquals("燃料は残り約3周", SpeechEvent.AceWindowsRemainingFuelLapsWarning(laps = 3).narratedText)
    }

    @Test
    fun `AceWindowsRemainingFuelLapsWarningはlapsが0以下のとき燃料切れの文言を返す`() {
        assertEquals("燃料がありません", SpeechEvent.AceWindowsRemainingFuelLapsWarning(laps = 0).narratedText)
        assertEquals("燃料がありません", SpeechEvent.AceWindowsRemainingFuelLapsWarning(laps = -1).narratedText)
    }

    @Test
    fun `AceWindowsRemainingFuelLapsWarningのreadoutItemKeyはACEの燃料残り周回数`() {
        assertEquals(
            ReadoutItemKey.AceWindows.RemainingFuelLaps.Root,
            SpeechEvent.AceWindowsRemainingFuelLapsWarning(laps = 3).readoutItemKey,
        )
    }

    @Test
    fun `ACEフラッグ系のnarratedTextはChipと同じ文言を返す`() {
        assertEquals("残り燃料警告", SpeechEvent.AceWindowsRemainingFuelWarning.narratedText)
        assertEquals("ホワイトフラッグ", SpeechEvent.AceWindowsWhiteFlag.narratedText)
        assertEquals("グリーンフラッグ", SpeechEvent.AceWindowsGreenFlag.narratedText)
        assertEquals("レッドフラッグ", SpeechEvent.AceWindowsRedFlag.narratedText)
        assertEquals("ブルーフラッグ", SpeechEvent.AceWindowsBlueFlag.narratedText)
        assertEquals("イエローフラッグ", SpeechEvent.AceWindowsYellowFlag.narratedText)
        assertEquals("ブラックフラッグ", SpeechEvent.AceWindowsBlackFlag.narratedText)
        assertEquals("ブラック・ホワイトフラッグ", SpeechEvent.AceWindowsBlackWhiteFlag.narratedText)
        assertEquals("チェッカーフラッグ", SpeechEvent.AceWindowsCheckeredFlag.narratedText)
        assertEquals("オレンジボールフラッグ", SpeechEvent.AceWindowsOrangeCircleFlag.narratedText)
        assertEquals("レッド・イエローストライプフラッグ", SpeechEvent.AceWindowsRedYellowStripesFlag.narratedText)
    }

    @Test
    fun `ACEタイヤ・車両接近系のnarratedTextはChipと同じ文言を返す`() {
        assertEquals("タイヤ過熱警告", SpeechEvent.AceWindowsTyreOverheat.narratedText)
        assertEquals("車両接近", SpeechEvent.AceWindowsVehicleApproach.narratedText)
    }

    @Test
    fun `燃料残り周回数イベントは解決文言を保持しログ用の既定文言は維持する`() {
        val event = SpeechEvent.RemainingFuelLapsWarning(1)
        assertEquals(null, event.resolvedText)
        val resolved = event.copy(resolvedText = "あと1周")
        assertEquals("あと1周", resolved.resolvedText)
        assertEquals("燃料は残り約1周", resolved.narratedText)
        assertEquals(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root, resolved.readoutItemKey)
    }

    @Test
    fun `GT7タイヤ過熱は既定文言とキーを維持し解決文言を保持する`() {
        val event = SpeechEvent.Gt7Ps5TyreOverheat(0, "設定文言")
        assertEquals("タイヤ過熱 0度", event.narratedText)
        assertEquals(ReadoutItemKey.Gt7Ps5.TyreTemperature.Root, event.readoutItemKey)
        assertEquals("設定文言", event.resolvedText)
    }
}
