package kurou.kodriver.domain.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class SpeechEventTest {
    @Test
    fun `LMU車両接近系のnarratedTextはChipと同じ文言を返す`() {
        assertEquals("カーレフト", SpeechEvent.CarLeft.narratedText)
        assertEquals("カーライト", SpeechEvent.CarRight.narratedText)
        assertEquals("左接近", SpeechEvent.LeftApproach.narratedText)
        assertEquals("右接近", SpeechEvent.RightApproach.narratedText)
        assertEquals("キープレフト", SpeechEvent.KeepLeft.narratedText)
        assertEquals("キープライト", SpeechEvent.KeepRight.narratedText)
        assertEquals("左側維持", SpeechEvent.LeftSustained.narratedText)
        assertEquals("右側維持", SpeechEvent.RightSustained.narratedText)
    }

    @Test
    fun `LMUフラッグ系のnarratedTextはChipと同じ文言を返す`() {
        assertEquals("ブルーフラッグ", SpeechEvent.BlueFlag.narratedText)
        assertEquals("イエローフラッグ", SpeechEvent.YellowFlag.narratedText)
        assertEquals("フルコースイエロー", SpeechEvent.FullCourseYellow.narratedText)
        assertEquals("セッションストップ", SpeechEvent.SessionStop.narratedText)
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
        assertEquals("タイヤ過熱警告", SpeechEvent.TyreOverheat.narratedText)
        assertEquals("タイヤ低温警告", SpeechEvent.TyreCold.narratedText)
        assertEquals("タイヤ摩耗警告", SpeechEvent.TyreWearWarning.narratedText)
        assertEquals("バーチャルエナジー残量警告", SpeechEvent.RemainingVirtualEnergyWarning.narratedText)
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
    fun `Gt7Ps5の燃料・タイヤ系のnarratedTextはChipと同じ文言を返す`() {
        assertEquals("残り燃料警告", SpeechEvent.Gt7Ps5RemainingFuelWarning.narratedText)
        assertEquals("タイヤ過熱警告", SpeechEvent.Gt7Ps5TyreOverheat.narratedText)
    }

    @Test
    fun `PitTimingWarningはlapsが1以上のとき残り周回数の文言を返す`() {
        assertEquals("残り約2周でピットイン", SpeechEvent.PitTimingWarning(laps = 2).narratedText)
    }

    @Test
    fun `PitTimingWarningはlapsが0以下のとき必ずピットインの文言を返す`() {
        assertEquals("必ずピットイン", SpeechEvent.PitTimingWarning(laps = 0).narratedText)
        assertEquals("必ずピットイン", SpeechEvent.PitTimingWarning(laps = -1).narratedText)
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
}
