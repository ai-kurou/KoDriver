package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutTextDefaultsTest {
    @Test
    fun `前後の空白を除去する`() {
        assertEquals("レッドフラッグ、セッション中断", "  レッドフラッグ、セッション中断  ".normalizeReadoutCustomText())
    }

    @Test
    fun `空白のみの入力は空文字にする`() {
        assertEquals("", "   ".normalizeReadoutCustomText())
    }

    @Test
    fun `空文字は空文字のまま返す`() {
        assertEquals("", "".normalizeReadoutCustomText())
    }

    @Test
    fun `最大文字数を超える入力は切り詰める`() {
        val text = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5)

        assertEquals("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH), text.normalizeReadoutCustomText())
    }

    @Test
    fun `最大文字数ちょうどの入力はそのまま返す`() {
        val text = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH)

        assertEquals(text, text.normalizeReadoutCustomText())
    }

    @Test
    fun `空白除去後に最大文字数へ収まる入力は切り詰めない`() {
        val body = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH)

        assertEquals(body, " $body ".normalizeReadoutCustomText())
    }
}
