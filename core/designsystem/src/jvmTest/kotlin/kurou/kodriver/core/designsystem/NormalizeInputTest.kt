package kurou.kodriver.core.designsystem

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NormalizeInputTest {
    @Test
    fun `改行を除去し選択範囲を除去後のオフセットへ写像する`() {
        val actual = normalizeInput(TextFieldValue("a\nb\nc", TextRange(2, 4)), 10)

        assertEquals("abc", actual.text)
        assertEquals(TextRange(1, 2), actual.selection)
    }

    @Test
    fun `上限で切り詰めた場合は選択範囲を切り詰め後の長さに収める`() {
        val actual = normalizeInput(TextFieldValue("abcdef", TextRange(6)), 4)

        assertEquals("abcd", actual.text)
        assertEquals(TextRange(4), actual.selection)
    }

    @Test
    fun `加工しない入力では変換中範囲を引き継ぐ`() {
        val actual = normalizeInput(TextFieldValue("あいう", TextRange(3), TextRange(1, 3)), 10)

        assertEquals(TextRange(1, 3), actual.composition)
    }

    @Test
    fun `加工した入力では変換中範囲を破棄する`() {
        val actual = normalizeInput(TextFieldValue("あ\nいう", TextRange(4), TextRange(2, 4)), 10)

        assertNull(actual.composition)
    }
}
