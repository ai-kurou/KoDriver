@file:Suppress("TooManyFunctions")

package kurou.kodriver.domain.engine

import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.PitTimingSource
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LmuWindowsSpeechEventTest {
    @BeforeTest
    fun registerReadoutKeys() {
        registerLmuWindowsReadoutItemKeys()
    }

    @Test
    fun `LMUの現在のブレーキ残量は既定文言とRootキーを持ち解決済み文言を反映する`() {
        val event = LmuWindowsBrakeWearCurrentValue(42)

        assertEquals(42, event.percentage)

        assertEquals("ブレーキ残量42%", event.narratedText)

        assertEquals(LmuWindowsReadoutItemKey.BrakeWear.Root, event.readoutItemKey)

        assertEquals("残り42%", event.withResolvedText("残り42%").narratedText)

        assertEquals("", event.withResolvedText("").narratedText)
    }

    @Test
    fun `LMUイベントだけがLMU由来の共通マーカーを持つ`() {
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
                LmuWindowsBrakeWearLow(30),
                LmuWindowsRemainingVirtualEnergyWarning(50),
                LmuWindowsRemainingVirtualEnergyCurrentValue(42),
                LmuWindowsManualReadoutText(LmuWindowsReadoutItemKey.TyreWear.Root, "連結本文"),
                LmuWindowsMyBestLap(83_456L),
                LmuWindowsPitTimingWarning(2, PitTimingSource.TyreWear),
            )

        events.forEach { event ->

            assertTrue(event is LmuWindowsSpeechEvent)

            assertTrue(event.withResolvedText("判定時の本文") is LmuWindowsSpeechEvent)
        }

        val debugEvent: SpeechEvent = DebugReadoutText(LmuWindowsReadoutItemKey.Flag.Root, "入力文言")
        assertFalse(debugEvent is LmuWindowsSpeechEvent)
    }

    @Test
    fun `LMUの全自由文字列イベントは解決済み本文を保持して型とキーを維持する`() {
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
                LmuWindowsRemainingVirtualEnergyCurrentValue(42),
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
                LmuWindowsRemainingVirtualEnergyCurrentValue(42, "判定時の本文"),
                LmuWindowsMyBestLap(83_456L, "判定時の本文"),
                LmuWindowsPitTimingWarning(2, PitTimingSource.TyreWear, "判定時の本文"),
            )

        assertEquals(expected, events.map { it.withResolvedText("判定時の本文") })

        events.forEach { event ->

            assertEquals(null, event.resolvedText)

            val resolved = event.withResolvedText("判定時の本文")

            assertEquals(event::class, resolved::class)

            assertEquals("判定時の本文", resolved.resolvedText)

            assertEquals(
                if (event is LmuWindowsRemainingVirtualEnergyCurrentValue) "判定時の本文" else event.narratedText,
                resolved.narratedText,
            )

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
    fun `LMUタイヤ・エネルギー系のnarratedTextはChipと同じ文言を返す`() {
        assertEquals("ブレーキ温度800℃以上", LmuWindowsBrakeOverheat(800).narratedText)

        assertEquals("タイヤ過熱 100度", LmuWindowsTyreOverheat(100).narratedText)

        assertEquals("タイヤ低温 60度", LmuWindowsTyreCold(60).narratedText)

        assertEquals("タイヤ残存率50%以下", LmuWindowsTyreWearWarning(50).narratedText)

        assertEquals("バーチャルエネルギー残量50%以下", LmuWindowsRemainingVirtualEnergyWarning(50).narratedText)
    }

    @Test
    fun `LMUの現在のエネルギー残量は既定文言と既存のRootキーを持ち解決済み文言を反映する`() {
        val event = LmuWindowsRemainingVirtualEnergyCurrentValue(42)

        assertEquals("残量42%", event.withResolvedText("残量42%").narratedText)

        assertEquals("", event.withResolvedText("").narratedText)

        assertEquals(42, event.percentage)

        assertEquals("バーチャルエネルギー残量は42パーセント", event.narratedText)

        assertEquals(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root, event.readoutItemKey)
    }

    @Test
    fun `LMUの手動連結イベントは指定キーと本文を保持し解決済み文言を優先する`() {
        listOf(
            LmuWindowsReadoutItemKey.PitTiming.Root,
            LmuWindowsReadoutItemKey.MyBestLap.Root,
            LmuWindowsReadoutItemKey.BrakeTemperature.Root,
            LmuWindowsReadoutItemKey.TyreTemperature.Root,
            LmuWindowsReadoutItemKey.TyreWear.Root,
            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
        ).forEach { key ->

            val event = LmuWindowsManualReadoutText(key, "タイヤ80%、VE50%")

            assertEquals(key, event.readoutItemKey)

            assertEquals(null, event.resolvedText)

            assertEquals("タイヤ80%、VE50%", event.narratedText)

            assertEquals("解決済み", event.withResolvedText("解決済み").narratedText)

            assertEquals("", event.withResolvedText("").narratedText)

            assertEquals(" ", event.withResolvedText(" ").narratedText)

            assertEquals(event, event.withResolvedText("解決済み").copy(resolvedText = null))
        }
    }

    @Test
    fun `LMUのタイヤ温度警告は温度と解決済み文言を保持し既定文言とキーを維持する`() {
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
    fun `LMUの自己ベストラップは既定文言を返す`() {
        assertEquals("自己ベストラップ更新 1分23秒456", LmuWindowsMyBestLap(83_456L).narratedText)

        assertEquals("自己ベストラップ更新 23秒005", LmuWindowsMyBestLap(23_005L, "カスタム").narratedText)
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
    fun `LMUのバーチャルエネルギーの1以上の周回数は通常既定文言を置換する`() {
        listOf(1, 3).forEach { laps ->

            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT.replace("{laps}", laps.toString()),
                LmuWindowsPitTimingWarning(laps, PitTimingSource.VirtualEnergy).narratedText,
            )
        }
    }

    @Test
    fun `LMUのバーチャルエネルギーの0以下の周回数は切迫既定文言を返す`() {
        listOf(0, -1).forEach { laps ->

            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT,
                LmuWindowsPitTimingWarning(laps, PitTimingSource.VirtualEnergy).narratedText,
            )
        }
    }

    @Test
    fun `LMUのタイヤ摩耗の1以上の周回数は通常既定文言を置換する`() {
        listOf(1, 3).forEach { laps ->

            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT.replace("{laps}", laps.toString()),
                LmuWindowsPitTimingWarning(laps, PitTimingSource.TyreWear).narratedText,
            )
        }
    }

    @Test
    fun `LMUのタイヤ摩耗の0以下の周回数は切迫既定文言を返す`() {
        listOf(0, -1).forEach { laps ->

            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT,
                LmuWindowsPitTimingWarning(laps, PitTimingSource.TyreWear).narratedText,
            )
        }
    }

    @Test
    fun `LMUの車両故障イベントは解決済み文言を保持し既定文言とRootキーを維持する`() {
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
}
