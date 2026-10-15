package kurou.kodriver.core.designsystem

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InsertAtSelectionTest {
    @Test
    fun `先頭と中間と末尾に挿入して直後にカーソルを置く`() {
        assertEquals(TextFieldValue("Xabcd", TextRange(1)), insertAtSelection("abcd", TextRange(0), "X", 5))
        assertEquals(TextFieldValue("abXcd", TextRange(3)), insertAtSelection("abcd", TextRange(2), "X", 5))
        assertEquals(TextFieldValue("abcdX", TextRange(5)), insertAtSelection("abcd", TextRange(4), "X", 5))
    }

    @Test
    fun `選択方向によらず選択範囲を置換する`() {
        val expected = TextFieldValue("aXYd", TextRange(3))
        assertEquals(expected, insertAtSelection("abcd", TextRange(1, 3), "XY", 4))
        assertEquals(expected, insertAtSelection("abcd", TextRange(3, 1), "XY", 4))
    }

    @Test
    fun `上限ちょうどは挿入でき超過は挿入しない`() {
        assertEquals(TextFieldValue("abcXY", TextRange(5)), insertAtSelection("abc", TextRange(3), "XY", 5))
        assertNull(insertAtSelection("abc", TextRange(3), "XYZ", 5))
        assertEquals(TextFieldValue("", TextRange(0)), insertAtSelection("abc", TextRange(0, 3), "", 0))
    }

    @Test
    fun `既存文字列と挿入文字列の改行を除去してカーソル位置と上限を計算する`() {
        assertEquals(
            TextFieldValue("aXYb", TextRange(3)),
            insertAtSelection("a\nb\n", TextRange(2), "X\nY\n", 4),
        )
    }

    @Test
    fun `文字列の長さを超えた選択範囲は末尾に収める`() {
        assertEquals(TextFieldValue("aX", TextRange(2)), insertAtSelection("abc", TextRange(5, 1), "X", 3))
    }
}
