@file:Suppress("TooManyFunctions")

package kurou.kodriver.domain.engine

import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AceWindowsSpeechEventTest {
    @BeforeTest
    fun registerReadoutKeys() {
        registerAceWindowsReadoutItemKeys()
    }

    @Test
    fun `ACEの手動タイヤ温度はRootキーと既定文言と解決済み本文を持つ`() {
        val event = AceWindowsTyreTemperatureCurrentValue(90)

        assertEquals(AceWindowsReadoutItemKey.TyreTemperature.Root, event.readoutItemKey)

        assertEquals(90, event.celsius)

        assertEquals("タイヤ温度90℃", event.narratedText)

        assertEquals("温度90", event.withResolvedText("温度90").narratedText)

        assertEquals("", event.withResolvedText("").narratedText)
    }

    @Test
    fun `ACEの手動文言はキーと本文を保持し解決済み文言を優先する`() {
        val key = AceWindowsReadoutItemKey.RemainingFuel.Root

        val event = AceWindowsManualReadoutText(key, "読み上げる項目がありません")

        assertEquals(key, event.readoutItemKey)

        assertEquals("読み上げる項目がありません", event.narratedText)

        val resolved = event.withResolvedText("解決済み")

        assertEquals("解決済み", resolved.resolvedText)

        assertEquals("解決済み", resolved.narratedText)

        assertEquals(event.text, resolved.text)

        assertEquals(key, resolved.readoutItemKey)

        assertEquals("", event.withResolvedText("").narratedText)
    }

    @Test
    fun `ACEの手動燃料残量はRootキーと既定文言と解決済み本文を持つ`() {
        val event = AceWindowsRemainingFuelCurrentValue(42)

        assertEquals(AceWindowsReadoutItemKey.RemainingFuel.Root, event.readoutItemKey)

        assertEquals("燃料は残り42パーセント", event.narratedText)

        val resolved = event.withResolvedText("残り42%")

        assertEquals("残り42%", resolved.resolvedText)

        assertEquals("残り42%", resolved.narratedText)

        assertEquals(42, resolved.percent)

        assertEquals("", event.withResolvedText("").narratedText)
    }

    @Test
    fun `ACEの手動燃料周回数はRootキーと既定文言と解決済み本文を持つ`() {
        val event = AceWindowsRemainingFuelLapsCurrentValue(5)

        assertEquals(AceWindowsReadoutItemKey.RemainingFuelLaps.Root, event.readoutItemKey)

        assertEquals("燃料は残り約5周", event.narratedText)

        val resolved = event.withResolvedText("約5周")

        assertEquals("約5周", resolved.resolvedText)

        assertEquals("約5周", resolved.narratedText)

        assertEquals(5, resolved.laps)

        assertEquals("", event.withResolvedText("").narratedText)
    }

    @Test
    fun `ACEの手動自己ベストラップイベントはRootキーと既定文言を持つ`() {
        val event = AceWindowsMyBestLapCurrentValue(83_456)

        assertEquals(AceWindowsReadoutItemKey.MyBestLap.Root, event.readoutItemKey)

        assertEquals("自己ベストラップ 1分23秒456", event.narratedText)

        assertEquals("PB", event.withResolvedText("PB").narratedText)

        assertEquals(83_456, event.lapTimeMs)
    }

    @Test
    fun `ACEイベントだけがACE由来の共通マーカーを持つ`() {
        val events =

            listOf<ReadoutTextEvent>(
                AceWindowsRemainingFuelLapsWarning(3),
                AceWindowsRemainingFuelWarning(20),
                AceWindowsWhiteFlag(),
                AceWindowsGreenFlag(),
                AceWindowsRedFlag(),
                AceWindowsBlueFlag(),
                AceWindowsYellowFlag(),
                AceWindowsBlackFlag(),
                AceWindowsBlackWhiteFlag(),
                AceWindowsCheckeredFlag(),
                AceWindowsOrangeCircleFlag(),
                AceWindowsRedYellowStripesFlag(),
                AceWindowsTyreOverheat(110),
                AceWindowsVehicleApproach(),
                AceWindowsMyBestLap(83_456),
            )

        events.forEach { event ->

            assertTrue(event is AceWindowsSpeechEvent)

            assertTrue(event.withResolvedText("判定時の本文") is AceWindowsSpeechEvent)
        }

        val debugEvent: SpeechEvent = DebugReadoutText(AceWindowsReadoutItemKey.Flag.Root, "入力文言")
        assertFalse(debugEvent is AceWindowsSpeechEvent)
    }

    @Test
    fun `ACEの自己ベストラップは既定文言とキーを保つ`() {
        val event = AceWindowsMyBestLap(83_456)

        assertEquals("自己ベストラップ更新 1分23秒456", event.narratedText)

        assertEquals(AceWindowsReadoutItemKey.MyBestLap.Root, event.readoutItemKey)

        assertEquals(83_456, event.lapTimeMs)

        assertEquals(null, event.resolvedText)

        assertEquals("更新済み", event.withResolvedText("更新済み").resolvedText)

        assertEquals("自己ベストラップ更新 1分23秒456", event.withResolvedText("更新済み").narratedText)
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
