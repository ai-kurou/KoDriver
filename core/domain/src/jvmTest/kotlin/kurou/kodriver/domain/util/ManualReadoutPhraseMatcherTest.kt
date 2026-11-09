package kurou.kodriver.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ManualReadoutPhraseMatcherTest {
    private val phrases =
        mapOf(
            "fuel" to listOf("燃料", "ガソリン"),
            "laps" to listOf("燃料周回", "燃料はあと何周"),
            "ve" to listOf("VE"),
            "behind" to listOf("後方"),
            "brake" to listOf("ブレーキ温度"),
        )

    @Test
    fun `語句に一致する発話は対応する項目キーを返す`() {
        assertEquals("fuel", matchManualReadoutItemKey(listOf("ガソリンを教えて"), phrases))
    }

    @Test
    fun `複数項目に一致する場合は最も長い語句の項目を選ぶ`() {
        assertEquals("laps", matchManualReadoutItemKey(listOf("燃料周回を教えて"), phrases))
        assertEquals("laps", matchManualReadoutItemKey(listOf("燃料はあと何周"), phrases))
    }

    @Test
    fun `英字だけの語句は英単語の途中には一致せず単独や日本語に隣接する場合は一致する`() {
        assertNull(matchManualReadoutItemKey(listOf("have a nice day"), phrases))
        assertEquals("ve", matchManualReadoutItemKey(listOf("VEを教えて"), phrases))
        assertEquals("ve", matchManualReadoutItemKey(listOf("have", "VE"), phrases))
    }

    @Test
    fun `空白と英字の大文字小文字を無視する`() {
        assertEquals("ve", matchManualReadoutItemKey(listOf("v e"), phrases))
        assertEquals("fuel", matchManualReadoutItemKey(listOf("燃 料"), phrases))
    }

    @Test
    fun `一致した語句の長さが同じ場合は先頭に近い候補を採用する`() {
        assertEquals("fuel", matchManualReadoutItemKey(listOf("関係ない言葉", "燃料", "VE"), phrases))
    }

    @Test
    fun `第1候補が短い語句でも下位候補のより長い語句の項目を選ぶ`() {
        assertEquals("laps", matchManualReadoutItemKey(listOf("燃料", "燃料周回数"), phrases))
        assertEquals("laps", matchManualReadoutItemKey(listOf("関係ない言葉", "燃料", "燃料周回"), phrases))
    }

    @Test
    fun `下位候補の無関係な項目は語句が長くても第1候補に勝てない`() {
        assertEquals("behind", matchManualReadoutItemKey(listOf("後方", "ブレーキ温度"), phrases))
        assertEquals("ve", matchManualReadoutItemKey(listOf("VE", "燃料周回"), phrases))
    }

    @Test
    fun `下位候補では現在の最良語句を含む最も長い語句の項目を選ぶ`() {
        assertEquals("laps", matchManualReadoutItemKey(listOf("燃料", "燃料周回とブレーキ温度"), phrases))
        assertEquals("fuel", matchManualReadoutItemKey(listOf("燃料", "燃料"), phrases))
    }

    @Test
    fun `一致しない・空の候補はnull`() {
        assertNull(matchManualReadoutItemKey(emptyList(), phrases))
        assertNull(matchManualReadoutItemKey(listOf("", "  "), phrases))
        assertNull(matchManualReadoutItemKey(listOf("こんにちは"), phrases))
        assertNull(matchManualReadoutItemKey(listOf("燃料"), emptyMap()))
    }
}
