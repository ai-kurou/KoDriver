package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutDefaultsTest {
    @Test
    fun `上限以内の名前はそのまま返す`() {
        assertEquals("ABC", "ABC".takeManualReadoutSetName())
    }

    @Test
    fun `上限を超える名前は上限文字数に切り詰める`() {
        val name = "あ".repeat(MANUAL_READOUT_SET_NAME_MAX_LENGTH + 5)

        assertEquals("あ".repeat(MANUAL_READOUT_SET_NAME_MAX_LENGTH), name.takeManualReadoutSetName())
    }

    @Test
    fun `上限境界でサロゲートペアを分割しない`() {
        val name = "a".repeat(MANUAL_READOUT_SET_NAME_MAX_LENGTH - 1) + "🏁"

        assertEquals("a".repeat(MANUAL_READOUT_SET_NAME_MAX_LENGTH - 1), name.takeManualReadoutSetName())
    }

    @Test
    fun `上限ちょうどに収まるサロゲートペアは残す`() {
        val name = "a".repeat(MANUAL_READOUT_SET_NAME_MAX_LENGTH - 2) + "🏁"

        assertEquals(name, name.takeManualReadoutSetName())
    }

    @Test
    fun `改行は除去する`() {
        assertEquals("ABCD", "A\nB\r\nC\rD".takeManualReadoutSetName())
    }
}
