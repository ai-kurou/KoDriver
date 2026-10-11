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
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.PitTimingSource
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("TooManyFunctions")
class SpeechEventTest {
    @Test
    fun `全自由文字列イベントは本文を置き換えて型と既定文言とキーを維持する`() {
        val events =
            listOf<ReadoutTextEvent>(
                LmuWindowsCarLeft(),
                LmuWindowsCarRight(),
                LmuWindowsCarLeftSustained(),
                LmuWindowsCarRightSustained(),
                LmuWindowsBlueFlag(),
                LmuWindowsYellowFlag(),
                LmuWindowsFullCourseYellow(),
                LmuWindowsRedFlag(),
                LmuWindowsOverheating(),
                LmuWindowsPartDetached(),
                LmuWindowsTyreDetached(),
                LmuWindowsTyreOverheat(100),
                LmuWindowsTyreCold(60),
                LmuWindowsTyreWearWarning(50),
                LmuWindowsBrakeOverheat(700),
                LmuWindowsRemainingVirtualEnergyWarning(50),
                LmuWindowsMyBestLap(83_456L),
                LmuWindowsPitTimingWarning(2, PitTimingSource.TyreWear),
            )
        val expected =
            listOf<ReadoutTextEvent>(
                LmuWindowsCarLeft("判定時の本文"),
                LmuWindowsCarRight("判定時の本文"),
                LmuWindowsCarLeftSustained("判定時の本文"),
                LmuWindowsCarRightSustained("判定時の本文"),
                LmuWindowsBlueFlag("判定時の本文"),
                LmuWindowsYellowFlag("判定時の本文"),
                LmuWindowsFullCourseYellow("判定時の本文"),
                LmuWindowsRedFlag("判定時の本文"),
                LmuWindowsOverheating("判定時の本文"),
                LmuWindowsPartDetached("判定時の本文"),
                LmuWindowsTyreDetached("判定時の本文"),
                LmuWindowsTyreOverheat(100, "判定時の本文"),
                LmuWindowsTyreCold(60, "判定時の本文"),
                LmuWindowsTyreWearWarning(50, "判定時の本文"),
                LmuWindowsBrakeOverheat(700, "判定時の本文"),
                LmuWindowsRemainingVirtualEnergyWarning(50, "判定時の本文"),
                LmuWindowsMyBestLap(83_456L, "判定時の本文"),
                LmuWindowsPitTimingWarning(2, PitTimingSource.TyreWear, "判定時の本文"),
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
        assertEquals("カーレフト", LmuWindowsCarLeft().narratedText)
        assertEquals("カーライト", LmuWindowsCarRight().narratedText)
        assertEquals("キープレフト", LmuWindowsCarRightSustained().narratedText)
        assertEquals("キープライト", LmuWindowsCarLeftSustained().narratedText)
    }

    @Test
    fun `LMUフラッグ系のnarratedTextは既定文言を返す`() {
        assertEquals("ブルーフラッグ", LmuWindowsBlueFlag().narratedText)
        assertEquals("イエローフラッグ", LmuWindowsYellowFlag().narratedText)
        assertEquals("フルコースイエロー", LmuWindowsFullCourseYellow().narratedText)
        assertEquals("レッドフラッグ", LmuWindowsRedFlag().narratedText)
    }

    @Test
    fun `LMU車両故障系のnarratedTextは既定文言を返す`() {
        assertEquals("オーバーヒート", LmuWindowsOverheating().narratedText)
        assertEquals("部品脱落", LmuWindowsPartDetached().narratedText)
        assertEquals("タイヤ脱落", LmuWindowsTyreDetached().narratedText)
    }

    @Test
    fun `LMUタイヤ・エナジー系のnarratedTextはChipと同じ文言を返す`() {
        assertEquals("ブレーキ温度800℃以上", LmuWindowsBrakeOverheat(800).narratedText)
        assertEquals("タイヤ過熱 100度", LmuWindowsTyreOverheat(100).narratedText)
        assertEquals("タイヤ低温 60度", LmuWindowsTyreCold(60).narratedText)
        assertEquals("タイヤ残存率50%以下", LmuWindowsTyreWearWarning(50).narratedText)
        assertEquals("バーチャルエナジー残量50%以下", LmuWindowsRemainingVirtualEnergyWarning(50).narratedText)
    }

    @Test
    fun `タイヤ温度警告は温度と解決済み文言を保持し既定文言とキーを維持する`() {
        val overheat = LmuWindowsTyreOverheat(100)
        val cold = LmuWindowsTyreCold(60)
        assertEquals(null, overheat.resolvedText)
        assertEquals(null, cold.resolvedText)
        assertEquals(100, overheat.celsius)
        assertEquals(60, cold.celsius)
        assertEquals(LmuWindowsReadoutItemKey.TyreTemperature.Root, overheat.readoutItemKey)
        assertEquals(LmuWindowsReadoutItemKey.TyreTemperature.Root, cold.readoutItemKey)
        assertEquals("過熱100℃", overheat.copy(resolvedText = "過熱100℃").resolvedText)
        assertEquals("低温60℃", cold.copy(resolvedText = "低温60℃").resolvedText)
        assertEquals("タイヤ過熱 100度", overheat.copy(resolvedText = "過熱100℃").narratedText)
        assertEquals("タイヤ低温 60度", cold.copy(resolvedText = "低温60℃").narratedText)
    }

    @Test
    fun `自己ベストラップ系はシミュレーターごとの既定文言を返す`() {
        assertEquals("自己ベストラップ更新 1分23秒456", LmuWindowsMyBestLap(83_456L).narratedText)
        assertEquals("自己ベストラップ更新 23秒005", LmuWindowsMyBestLap(23_005L, "カスタム").narratedText)
        val gt7Event = Gt7Ps5MyBestLap(83_456)
        assertEquals("自己ベストラップ更新 1分23秒456", gt7Event.narratedText)
        assertEquals(Gt7Ps5ReadoutItemKey.MyBestLap.Root, gt7Event.readoutItemKey)
        assertEquals(83_456, gt7Event.lapTimeMs)
        assertEquals("更新済み", gt7Event.copy(resolvedText = "更新済み").resolvedText)
        assertEquals("自己ベストラップ更新 1分23秒456", gt7Event.copy(resolvedText = "更新済み").narratedText)
        val aceEvent = AceWindowsMyBestLap(83_456)
        assertEquals("自己ベストラップ更新 1分23秒456", aceEvent.narratedText)
        assertEquals(AceWindowsReadoutItemKey.MyBestLap.Root, aceEvent.readoutItemKey)
        assertEquals(83_456, aceEvent.lapTimeMs)
        assertEquals(null, aceEvent.resolvedText)
        assertEquals("更新済み", aceEvent.withResolvedText("更新済み").resolvedText)
        assertEquals("自己ベストラップ更新 1分23秒456", aceEvent.withResolvedText("更新済み").narratedText)
    }

    @Test
    fun `LMU自己ベストラップはLongのタイムと解決文言を保持しRootキーを維持する`() {
        val event = LmuWindowsMyBestLap(3_000_000_005L)
        assertEquals(3_000_000_005L, event.lapTimeMs)
        assertEquals(null, event.resolvedText)
        assertEquals(LmuWindowsReadoutItemKey.MyBestLap.Root, event.readoutItemKey)
        assertEquals("50000分0秒005", event.copy(resolvedText = "50000分0秒005").resolvedText)
        assertEquals("自己ベストラップ更新 50000分0秒005", event.copy(resolvedText = "カスタム").narratedText)
    }

    @Test
    fun `RemainingFuelLapsWarningはlapsが1以上のとき残り周回数の文言を返す`() {
        assertEquals("燃料は残り約3周", Gt7Ps5RemainingFuelLapsWarning(laps = 3).narratedText)
    }

    @Test
    fun `RemainingFuelLapsWarningはlapsが0以下のとき燃料切れの文言を返す`() {
        assertEquals("燃料残り1周未満", Gt7Ps5RemainingFuelLapsWarning(laps = 0).narratedText)
        assertEquals("燃料残り1周未満", Gt7Ps5RemainingFuelLapsWarning(laps = -1).narratedText)
    }

    @Test
    fun `Gt7Ps5燃料残量はRootキーと既定文言を持ち解決文言をcopyで保持する`() {
        val event = Gt7Ps5RemainingFuelWarning(29)
        val resolved = event.copy(resolvedText = "残り29%")

        assertEquals(Gt7Ps5ReadoutItemKey.RemainingFuel.Root, event.readoutItemKey)
        assertEquals("燃料は残り29パーセント", event.narratedText)
        assertEquals("燃料は残り29パーセント", resolved.narratedText)
        assertEquals("残り29%", resolved.resolvedText)
    }

    @Test
    fun `Gt7Ps5の燃料残量は整数を展開しタイヤは警告文言を返す`() {
        assertEquals("燃料は残り30パーセント", Gt7Ps5RemainingFuelWarning(30).narratedText)
        assertEquals("タイヤ過熱 107度", Gt7Ps5TyreOverheat(107).narratedText)
    }

    @Test
    fun `バーチャルエナジーの1以上の周回数は通常既定文言を置換する`() {
        listOf(1, 3).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT.replace("{laps}", laps.toString()),
                LmuWindowsPitTimingWarning(laps, PitTimingSource.VirtualEnergy).narratedText,
            )
        }
    }

    @Test
    fun `バーチャルエナジーの0以下の周回数は切迫既定文言を返す`() {
        listOf(0, -1).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT,
                LmuWindowsPitTimingWarning(laps, PitTimingSource.VirtualEnergy).narratedText,
            )
        }
    }

    @Test
    fun `タイヤ摩耗の1以上の周回数は通常既定文言を置換する`() {
        listOf(1, 3).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT.replace("{laps}", laps.toString()),
                LmuWindowsPitTimingWarning(laps, PitTimingSource.TyreWear).narratedText,
            )
        }
    }

    @Test
    fun `タイヤ摩耗の0以下の周回数は切迫既定文言を返す`() {
        listOf(0, -1).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT,
                LmuWindowsPitTimingWarning(laps, PitTimingSource.TyreWear).narratedText,
            )
        }
    }

    @Test
    fun `AceWindowsRemainingFuelLapsWarningはlapsが1以上のとき残り周回数の文言を返す`() {
        assertEquals("燃料は残り約3周", AceWindowsRemainingFuelLapsWarning(laps = 3).narratedText)
    }

    @Test
    fun `AceWindowsRemainingFuelLapsWarningはlapsが0以下のとき燃料切れの文言を返す`() {
        assertEquals("燃料残り1周未満", AceWindowsRemainingFuelLapsWarning(laps = 0).narratedText)
        assertEquals("燃料残り1周未満", AceWindowsRemainingFuelLapsWarning(laps = -1).narratedText)
    }

    @Test
    fun `AceWindowsRemainingFuelLapsWarningのreadoutItemKeyはACEの燃料残り周回数`() {
        assertEquals(
            AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
            AceWindowsRemainingFuelLapsWarning(laps = 3).readoutItemKey,
        )
    }

    @Test
    fun `ACEフラッグ系のnarratedTextは画面と同じ既定文言を返す`() {
        assertEquals("燃料は残り20パーセント", AceWindowsRemainingFuelWarning(20).narratedText)
        assertEquals("ホワイトフラッグ", AceWindowsWhiteFlag().narratedText)
        assertEquals("グリーンフラッグ", AceWindowsGreenFlag().narratedText)
        assertEquals("レッドフラッグ", AceWindowsRedFlag().narratedText)
        assertEquals("ブルーフラッグ", AceWindowsBlueFlag().narratedText)
        assertEquals("イエローフラッグ", AceWindowsYellowFlag().narratedText)
        assertEquals("ブラックフラッグ", AceWindowsBlackFlag().narratedText)
        assertEquals("ブラック・ホワイトフラッグ", AceWindowsBlackWhiteFlag().narratedText)
        assertEquals("チェッカーフラッグ", AceWindowsCheckeredFlag().narratedText)
        assertEquals(
            ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsCheckeredFlag().narratedText,
        )
        assertEquals(ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT, AceWindowsWhiteFlag().narratedText)
        assertEquals(ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT, AceWindowsGreenFlag().narratedText)
        assertEquals(ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT, AceWindowsRedFlag().narratedText)
        assertEquals(ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT, AceWindowsBlueFlag().narratedText)
        assertEquals(ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT, AceWindowsYellowFlag().narratedText)
        assertEquals(ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT, AceWindowsBlackFlag().narratedText)
        assertEquals(
            ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsBlackWhiteFlag().narratedText,
        )
        assertEquals(
            ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsOrangeCircleFlag().narratedText,
        )
        assertEquals(
            ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsRedYellowStripesFlag().narratedText,
        )
    }

    @Test
    fun `ACEタイヤと車両接近は既定文言を返す`() {
        assertEquals("タイヤ過熱 110度", AceWindowsTyreOverheat(110).narratedText)
        assertEquals(
            ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT,
            AceWindowsVehicleApproach().narratedText,
        )
        assertEquals(
            AceWindowsReadoutItemKey.VehicleApproach.Root,
            AceWindowsVehicleApproach().readoutItemKey,
        )
    }

    @Test
    fun `燃料残り周回数イベントは解決文言を保持しログ用の既定文言は維持する`() {
        val event = Gt7Ps5RemainingFuelLapsWarning(1)
        assertEquals(null, event.resolvedText)
        val resolved = event.copy(resolvedText = "あと1周")
        assertEquals("あと1周", resolved.resolvedText)
        assertEquals("燃料は残り約1周", resolved.narratedText)
        assertEquals(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root, resolved.readoutItemKey)
    }

    @Test
    fun `GT7タイヤ過熱は既定文言とキーを維持し解決文言を保持する`() {
        val event = Gt7Ps5TyreOverheat(0, "設定文言")
        assertEquals("タイヤ過熱 0度", event.narratedText)
        assertEquals(Gt7Ps5ReadoutItemKey.TyreTemperature.Root, event.readoutItemKey)
        assertEquals("設定文言", event.resolvedText)
    }

    @Test
    fun `車両故障イベントは解決済み文言を保持し既定文言とRootキーを維持する`() {
        val events =
            listOf(
                LmuWindowsOverheating("カスタム"),
                LmuWindowsPartDetached("カスタム"),
                LmuWindowsTyreDetached("カスタム"),
            )
        assertEquals("カスタム", (events[0] as LmuWindowsOverheating).resolvedText)
        assertEquals("カスタム", (events[1] as LmuWindowsPartDetached).resolvedText)
        assertEquals("カスタム", (events[2] as LmuWindowsTyreDetached).resolvedText)
        assertEquals(listOf("オーバーヒート", "部品脱落", "タイヤ脱落"), events.map { it.narratedText })
        events.forEach { assertEquals(LmuWindowsReadoutItemKey.VehicleDamage.Root, it.readoutItemKey) }
    }

    @Test
    fun `ACEタイヤ過熱は温度と判定時本文を保持する`() {
        val event = AceWindowsTyreOverheat(111)
        assertEquals(AceWindowsReadoutItemKey.TyreTemperature.Root, event.readoutItemKey)
        assertEquals(null, event.resolvedText)
        val resolved = event.withResolvedText("過熱注意")
        assertEquals(111, resolved.celsius)
        assertEquals("過熱注意", resolved.resolvedText)
        assertEquals("タイヤ過熱 111度", resolved.narratedText)
    }

    @Test
    fun `ACE燃料残量イベントは既定文言とRootキーと解決済み本文を保持する`() {
        val event = AceWindowsRemainingFuelWarning(30)
        assertEquals("燃料は残り30パーセント", event.narratedText)
        assertEquals(AceWindowsReadoutItemKey.RemainingFuel.Root, event.readoutItemKey)
        assertEquals(null, event.resolvedText)
        assertEquals(AceWindowsRemainingFuelWarning(30, "残り30%"), event.withResolvedText("残り30%"))
    }

    @Test
    fun `ACE燃料残り周回数は解決済み文言を保持し既定文言を維持する`() {
        val event = AceWindowsRemainingFuelLapsWarning(3)
        assertEquals(null, event.resolvedText)
        val resolved = event.withResolvedText("残り3周")
        assertEquals(AceWindowsRemainingFuelLapsWarning(3, "残り3周"), resolved)
        assertEquals("燃料は残り約3周", resolved.narratedText)
    }

    @Test
    fun `ACEフラッグと車両接近は解決済み本文を保持し既定文言を維持する`() {
        val events =
            listOf<ReadoutTextEvent>(
                AceWindowsCheckeredFlag(),
                AceWindowsWhiteFlag(),
                AceWindowsGreenFlag(),
                AceWindowsRedFlag(),
                AceWindowsBlueFlag(),
                AceWindowsYellowFlag(),
                AceWindowsBlackFlag(),
                AceWindowsBlackWhiteFlag(),
                AceWindowsOrangeCircleFlag(),
                AceWindowsRedYellowStripesFlag(),
                AceWindowsVehicleApproach(),
            )
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
}
