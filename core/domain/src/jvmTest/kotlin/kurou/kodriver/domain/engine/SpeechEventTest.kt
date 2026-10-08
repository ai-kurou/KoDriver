@file:Suppress("TooManyFunctions")

package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT
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
    fun `全自由文字列イベントは本文を置き換えて型と既定文言とキーを維持する`() {
        val events =
            listOf<FreeTextSpeechEvent>(
                SpeechEvent.CarLeft(),
                SpeechEvent.CarRight(),
                SpeechEvent.CarLeftSustained(),
                SpeechEvent.CarRightSustained(),
                SpeechEvent.BlueFlag(),
                SpeechEvent.YellowFlag(),
                SpeechEvent.FullCourseYellow(),
                SpeechEvent.RedFlag(),
                SpeechEvent.Overheating(),
                SpeechEvent.PartDetached(),
                SpeechEvent.TyreDetached(),
                SpeechEvent.TyreOverheat(100),
                SpeechEvent.TyreCold(60),
                SpeechEvent.TyreWearWarning(50),
                SpeechEvent.BrakeOverheat(700),
                SpeechEvent.RemainingVirtualEnergyWarning(50),
                SpeechEvent.LmuWindowsMyBestLap(83_456L),
                SpeechEvent.PitTimingWarning(2, PitTimingSource.TyreWear),
            )
        val expected =
            listOf<FreeTextSpeechEvent>(
                SpeechEvent.CarLeft("判定時の本文"),
                SpeechEvent.CarRight("判定時の本文"),
                SpeechEvent.CarLeftSustained("判定時の本文"),
                SpeechEvent.CarRightSustained("判定時の本文"),
                SpeechEvent.BlueFlag("判定時の本文"),
                SpeechEvent.YellowFlag("判定時の本文"),
                SpeechEvent.FullCourseYellow("判定時の本文"),
                SpeechEvent.RedFlag("判定時の本文"),
                SpeechEvent.Overheating("判定時の本文"),
                SpeechEvent.PartDetached("判定時の本文"),
                SpeechEvent.TyreDetached("判定時の本文"),
                SpeechEvent.TyreOverheat(100, "判定時の本文"),
                SpeechEvent.TyreCold(60, "判定時の本文"),
                SpeechEvent.TyreWearWarning(50, "判定時の本文"),
                SpeechEvent.BrakeOverheat(700, "判定時の本文"),
                SpeechEvent.RemainingVirtualEnergyWarning(50, "判定時の本文"),
                SpeechEvent.LmuWindowsMyBestLap(83_456L, "判定時の本文"),
                SpeechEvent.PitTimingWarning(2, PitTimingSource.TyreWear, "判定時の本文"),
            )
        assertEquals(expected, events.map { it.withResolvedText("判定時の本文") })
        events.forEach { event ->
            assertEquals(null, event.resolvedText)
            val resolved = event.withResolvedText("判定時の本文")
            assertEquals(event::class, resolved::class)
            assertEquals("判定時の本文", resolved.resolvedText)
            assertEquals(event.narratedText, resolved.narratedText)
            assertEquals(event.readoutItemKey, resolved.readoutItemKey)
            assertEquals("変更後の本文", resolved.withResolvedText("変更後の本文").resolvedText)
            assertEquals("", resolved.withResolvedText("").resolvedText)
        }
    }

    @Test
    fun `LMU車両接近系のnarratedTextは既定文言を返す`() {
        assertEquals("カーレフト", SpeechEvent.CarLeft().narratedText)
        assertEquals("カーライト", SpeechEvent.CarRight().narratedText)
        assertEquals("キープレフト", SpeechEvent.CarRightSustained().narratedText)
        assertEquals("キープライト", SpeechEvent.CarLeftSustained().narratedText)
    }

    @Test
    fun `LMUフラッグ系のnarratedTextは既定文言を返す`() {
        assertEquals("ブルーフラッグ", SpeechEvent.BlueFlag().narratedText)
        assertEquals("イエローフラッグ", SpeechEvent.YellowFlag().narratedText)
        assertEquals("フルコースイエロー", SpeechEvent.FullCourseYellow().narratedText)
        assertEquals("レッドフラッグ", SpeechEvent.RedFlag().narratedText)
    }

    @Test
    fun `LMU車両故障系のnarratedTextは既定文言を返す`() {
        assertEquals("オーバーヒート", SpeechEvent.Overheating().narratedText)
        assertEquals("部品脱落", SpeechEvent.PartDetached().narratedText)
        assertEquals("タイヤ脱落", SpeechEvent.TyreDetached().narratedText)
    }

    @Test
    fun `LMUタイヤ・エナジー系のnarratedTextはChipと同じ文言を返す`() {
        assertEquals("ブレーキ温度800℃以上", SpeechEvent.BrakeOverheat(800).narratedText)
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
    fun `自己ベストラップ系はシミュレーターごとの既定文言を返す`() {
        assertEquals("自己ベストラップ更新 1分23秒456", SpeechEvent.LmuWindowsMyBestLap(83_456L).narratedText)
        assertEquals("自己ベストラップ更新 23秒005", SpeechEvent.LmuWindowsMyBestLap(23_005L, "カスタム").narratedText)
        val gt7Event = SpeechEvent.Gt7Ps5MyBestLap(83_456)
        assertEquals("自己ベストラップ更新 1分23秒456", gt7Event.narratedText)
        assertEquals(ReadoutItemKey.Gt7Ps5.MyBestLap.Root, gt7Event.readoutItemKey)
        assertEquals(83_456, gt7Event.lapTimeMs)
        assertEquals("更新済み", gt7Event.copy(resolvedText = "更新済み").resolvedText)
        assertEquals("自己ベストラップ更新 1分23秒456", gt7Event.copy(resolvedText = "更新済み").narratedText)
        val aceEvent = SpeechEvent.AceWindowsMyBestLap(83_456)
        assertEquals("自己ベストラップ更新 1分23秒456", aceEvent.narratedText)
        assertEquals(ReadoutItemKey.AceWindows.MyBestLap.Root, aceEvent.readoutItemKey)
        assertEquals(83_456, aceEvent.lapTimeMs)
        assertEquals(null, aceEvent.resolvedText)
        assertEquals("更新済み", aceEvent.withResolvedText("更新済み").resolvedText)
        assertEquals("自己ベストラップ更新 1分23秒456", aceEvent.withResolvedText("更新済み").narratedText)
    }

    @Test
    fun `LMU自己ベストラップはLongのタイムと解決文言を保持しRootキーを維持する`() {
        val event = SpeechEvent.LmuWindowsMyBestLap(3_000_000_005L)
        assertEquals(3_000_000_005L, event.lapTimeMs)
        assertEquals(null, event.resolvedText)
        assertEquals(ReadoutItemKey.LmuWindows.MyBestLap.Root, event.readoutItemKey)
        assertEquals("50000分0秒005", event.copy(resolvedText = "50000分0秒005").resolvedText)
        assertEquals("自己ベストラップ更新 50000分0秒005", event.copy(resolvedText = "カスタム").narratedText)
    }

    @Test
    fun `RemainingFuelLapsWarningはlapsが1以上のとき残り周回数の文言を返す`() {
        assertEquals("燃料は残り約3周", SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(laps = 3).narratedText)
    }

    @Test
    fun `RemainingFuelLapsWarningはlapsが0以下のとき燃料切れの文言を返す`() {
        assertEquals("燃料残り1周未満", SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(laps = 0).narratedText)
        assertEquals("燃料残り1周未満", SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(laps = -1).narratedText)
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
        assertEquals("燃料残り1周未満", SpeechEvent.AceWindowsRemainingFuelLapsWarning(laps = 0).narratedText)
        assertEquals("燃料残り1周未満", SpeechEvent.AceWindowsRemainingFuelLapsWarning(laps = -1).narratedText)
    }

    @Test
    fun `AceWindowsRemainingFuelLapsWarningのreadoutItemKeyはACEの燃料残り周回数`() {
        assertEquals(
            ReadoutItemKey.AceWindows.RemainingFuelLaps.Root,
            SpeechEvent.AceWindowsRemainingFuelLapsWarning(laps = 3).readoutItemKey,
        )
    }

    @Test
    fun `ACEフラッグ系のnarratedTextは画面と同じ既定文言を返す`() {
        assertEquals("燃料は残り20パーセント", SpeechEvent.AceWindowsRemainingFuelWarning(20).narratedText)
        assertEquals("ホワイトフラッグ", SpeechEvent.AceWindowsWhiteFlag.narratedText)
        assertEquals("グリーンフラッグ", SpeechEvent.AceWindowsGreenFlag.narratedText)
        assertEquals("レッドフラッグ", SpeechEvent.AceWindowsRedFlag.narratedText)
        assertEquals("ブルーフラッグ", SpeechEvent.AceWindowsBlueFlag.narratedText)
        assertEquals("イエローフラッグ", SpeechEvent.AceWindowsYellowFlag.narratedText)
        assertEquals("ブラックフラッグ", SpeechEvent.AceWindowsBlackFlag.narratedText)
        assertEquals("ブラック・ホワイトフラッグ", SpeechEvent.AceWindowsBlackWhiteFlag.narratedText)
        assertEquals("チェッカーフラッグ", SpeechEvent.AceWindowsCheckeredFlag.narratedText)
        assertEquals(ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT, SpeechEvent.AceWindowsCheckeredFlag.narratedText)
        assertEquals(ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT, SpeechEvent.AceWindowsWhiteFlag.narratedText)
        assertEquals(ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT, SpeechEvent.AceWindowsGreenFlag.narratedText)
        assertEquals(ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT, SpeechEvent.AceWindowsRedFlag.narratedText)
        assertEquals(ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT, SpeechEvent.AceWindowsBlueFlag.narratedText)
        assertEquals(ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT, SpeechEvent.AceWindowsYellowFlag.narratedText)
        assertEquals(ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT, SpeechEvent.AceWindowsBlackFlag.narratedText)
        assertEquals(
            ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT,
            SpeechEvent.AceWindowsBlackWhiteFlag.narratedText,
        )
        assertEquals(
            ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT,
            SpeechEvent.AceWindowsOrangeCircleFlag.narratedText,
        )
        assertEquals(
            ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT,
            SpeechEvent.AceWindowsRedYellowStripesFlag.narratedText,
        )
    }

    @Test
    fun `ACEタイヤと車両接近は既定文言を返す`() {
        assertEquals("タイヤ過熱 110度", SpeechEvent.AceWindowsTyreOverheat(110).narratedText)
        assertEquals(
            ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT,
            SpeechEvent.AceWindowsVehicleApproach.narratedText,
        )
        assertEquals(
            ReadoutItemKey.AceWindows.VehicleApproach.Root,
            SpeechEvent.AceWindowsVehicleApproach.readoutItemKey,
        )
    }

    @Test
    fun `燃料残り周回数イベントは解決文言を保持しログ用の既定文言は維持する`() {
        val event = SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(1)
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

    @Test
    fun `車両故障イベントは解決済み文言を保持し既定文言とRootキーを維持する`() {
        val events =
            listOf(
                SpeechEvent.Overheating("カスタム"),
                SpeechEvent.PartDetached("カスタム"),
                SpeechEvent.TyreDetached("カスタム"),
            )
        assertEquals("カスタム", (events[0] as SpeechEvent.Overheating).resolvedText)
        assertEquals("カスタム", (events[1] as SpeechEvent.PartDetached).resolvedText)
        assertEquals("カスタム", (events[2] as SpeechEvent.TyreDetached).resolvedText)
        assertEquals(listOf("オーバーヒート", "部品脱落", "タイヤ脱落"), events.map { it.narratedText })
        events.forEach { assertEquals(ReadoutItemKey.LmuWindows.VehicleDamage.Root, it.readoutItemKey) }
    }

    @Test
    fun `ACEタイヤ過熱は温度と判定時本文を保持する`() {
        val event = SpeechEvent.AceWindowsTyreOverheat(111)
        assertEquals(ReadoutItemKey.AceWindows.TyreTemperature.Root, event.readoutItemKey)
        assertEquals(null, event.resolvedText)
        val resolved = event.withResolvedText("過熱注意")
        assertEquals(111, resolved.celsius)
        assertEquals("過熱注意", resolved.resolvedText)
        assertEquals("タイヤ過熱 111度", resolved.narratedText)
    }

    @Test
    fun `ACE燃料残量イベントは既定文言とRootキーと解決済み本文を保持する`() {
        val event = SpeechEvent.AceWindowsRemainingFuelWarning(30)
        assertEquals("燃料は残り30パーセント", event.narratedText)
        assertEquals(ReadoutItemKey.AceWindows.RemainingFuel.Root, event.readoutItemKey)
        assertEquals(null, event.resolvedText)
        assertEquals(SpeechEvent.AceWindowsRemainingFuelWarning(30, "残り30%"), event.withResolvedText("残り30%"))
    }

    @Test
    fun `ACE燃料残り周回数は解決済み文言を保持し既定文言を維持する`() {
        val event = SpeechEvent.AceWindowsRemainingFuelLapsWarning(3)
        assertEquals(null, event.resolvedText)
        val resolved = event.withResolvedText("残り3周")
        assertEquals(SpeechEvent.AceWindowsRemainingFuelLapsWarning(3, "残り3周"), resolved)
        assertEquals("燃料は残り約3周", resolved.narratedText)
    }
}
