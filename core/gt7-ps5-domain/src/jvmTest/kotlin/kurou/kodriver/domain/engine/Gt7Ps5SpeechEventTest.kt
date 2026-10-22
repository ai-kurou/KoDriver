@file:Suppress("TooManyFunctions")

package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Gt7Ps5SpeechEventTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerGt7Ps5ReadoutItemKeys()
    }

    @Test
    fun `GT7の手動タイヤ温度は既定文言と解決済み文言と空文言を扱う`() {
        val event = Gt7Ps5TyreTemperatureCurrentValue(90)

        assertEquals(Gt7Ps5ReadoutItemKey.TyreTemperature.Root, event.readoutItemKey)

        assertEquals("タイヤ温度90℃", event.narratedText)

        assertEquals("温度90℃", event.withResolvedText("温度90℃").narratedText)

        assertEquals("", event.withResolvedText("").narratedText)

        assertEquals(event, event.withResolvedText("変更").copy(resolvedText = null))
    }

    @Test
    fun `GT7の手動周回数は現在値と1周未満と解決済み文言を保持する`() {
        val event = Gt7Ps5RemainingFuelLapsCurrentValue(3)

        assertEquals(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root, event.readoutItemKey)

        assertEquals("燃料は残り約3周", event.narratedText)

        assertEquals("燃料残り1周未満", Gt7Ps5RemainingFuelLapsCurrentValue(0).narratedText)

        assertEquals("燃料残り1周未満", Gt7Ps5RemainingFuelLapsCurrentValue(-1).narratedText)

        val resolved = event.withResolvedText("あと3周")

        assertEquals("あと3周", resolved.narratedText)

        assertEquals("あと3周", resolved.resolvedText)

        assertEquals(3, resolved.laps)

        assertEquals("", event.withResolvedText("").narratedText)
    }

    @Test
    fun `GT7の手動燃料残量はRootキーと既定文言と解決済み本文を持つ`() {
        val event = Gt7Ps5RemainingFuelCurrentValue(42)

        assertEquals(Gt7Ps5ReadoutItemKey.RemainingFuel.Root, event.readoutItemKey)

        assertEquals("燃料は残り42パーセント", event.narratedText)

        val resolved = event.withResolvedText("残り42%")

        assertEquals("残り42%", resolved.resolvedText)

        assertEquals("残り42%", resolved.narratedText)

        assertEquals(42, resolved.percent)
    }

    @Test
    fun `GT7の手動文言はキーと本文を保持し解決済み文言を優先する`() {
        val key = Gt7Ps5ReadoutItemKey.RemainingFuel.Root

        val event = Gt7Ps5ManualReadoutText(key, "読み上げる項目がありません")

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
    fun `GT7の4型だけがGT7由来の共通マーカーを持つ`() {
        val events =

            listOf<ReadoutTextEvent>(
                Gt7Ps5MyBestLap(83_456),
                Gt7Ps5RemainingFuelLapsWarning(3),
                Gt7Ps5RemainingFuelWarning(20),
                Gt7Ps5TyreOverheat(110),
            )

        events.forEach { event ->

            assertTrue(event is Gt7Ps5SpeechEvent)

            assertTrue(event.withResolvedText("判定時の本文") is Gt7Ps5SpeechEvent)
        }

        val debugEvent: SpeechEvent = DebugReadoutText(Gt7Ps5ReadoutItemKey.MyBestLap.Root, "入力文言")
        assertFalse(debugEvent is Gt7Ps5SpeechEvent)
    }

    @Test
    fun `GT7の自己ベストラップは既定文言とキーを保つ`() {
        val event = Gt7Ps5MyBestLap(83_456)

        assertEquals("自己ベストラップ更新 1分23秒456", event.narratedText)

        assertEquals(Gt7Ps5ReadoutItemKey.MyBestLap.Root, event.readoutItemKey)

        assertEquals(83_456, event.lapTimeMs)

        assertEquals("更新済み", event.copy(resolvedText = "更新済み").resolvedText)

        assertEquals("自己ベストラップ更新 1分23秒456", event.copy(resolvedText = "更新済み").narratedText)
    }

    @Test
    fun `GT7のRemainingFuelLapsWarningはlapsが1以上のとき残り周回数の文言を返す`() {
        assertEquals("燃料は残り約3周", Gt7Ps5RemainingFuelLapsWarning(laps = 3).narratedText)
    }

    @Test
    fun `GT7のRemainingFuelLapsWarningはlapsが0以下のとき燃料切れの文言を返す`() {
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
    fun `GT7の燃料残り周回数イベントは解決文言を保持しログ用の既定文言は維持する`() {
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
}
