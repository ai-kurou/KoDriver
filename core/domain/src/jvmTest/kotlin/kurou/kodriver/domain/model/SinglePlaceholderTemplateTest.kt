package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SinglePlaceholderTemplateTest {
    private val template = SinglePlaceholderTemplate("{value}")

    @Test
    fun `プレースホルダーを値に置換する`() {
        assertEquals("残り10周", template.format("残り{value}周", "10"))
    }

    @Test
    fun `プレースホルダーがなければ文言を維持する`() {
        assertEquals("注意", template.format("注意", "10"))
    }

    @Test
    fun `複数のプレースホルダーを置換して未知のトークンを維持する`() {
        assertEquals("10と10と{other}", template.format("{value}と{value}と{other}", "10"))
    }

    @Test
    fun `未知のトークンを出現順で重複なく返す`() {
        assertEquals(listOf("{b}", "{a}"), template.findUnknown("{b}{value}{a}{b}"))
    }

    @Test
    fun `既知のトークンと閉じていない括弧は未知のトークンにならない`() {
        assertEquals(emptyList(), template.findUnknown("{value}{ open"))
    }
}
