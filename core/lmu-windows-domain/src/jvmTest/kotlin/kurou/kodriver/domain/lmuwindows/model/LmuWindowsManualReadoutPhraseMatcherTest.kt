package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LmuWindowsManualReadoutPhraseMatcherTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerLmuWindowsReadoutItemKeys()
    }

    @Test
    fun `語句に一致する発話は対応する項目キーを返す`() {
        assertEquals(
            LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("燃料を教えて")),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.MyBestLap.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("じこべ")),
        )
    }

    @Test
    fun `複数項目に一致する場合は最も長い語句の項目を選ぶ`() {
        assertEquals(
            LmuWindowsReadoutItemKey.RemainingFuel.Laps.value,
            matchLmuWindowsManualReadoutItemKey(listOf("燃料周回を教えて")),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value,
            matchLmuWindowsManualReadoutItemKey(listOf("バーチャルエネルギー周回")),
        )
    }

    @Test
    fun `空白と英字の大文字小文字を無視する`() {
        assertEquals(
            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("v e")),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.TyreTemperature.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("タイヤ 温度")),
        )
    }

    @Test
    fun `候補は先頭から見て最初に一致した候補を採用する`() {
        assertEquals(
            LmuWindowsReadoutItemKey.BrakeTemperature.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("関係ない言葉", "ブレーキ温度", "燃料")),
        )
    }

    @Test
    fun `一致しない・空の候補はnull`() {
        assertNull(matchLmuWindowsManualReadoutItemKey(emptyList()))
        assertNull(matchLmuWindowsManualReadoutItemKey(listOf("", "  ")))
        assertNull(matchLmuWindowsManualReadoutItemKey(listOf("こんにちは")))
    }
}
