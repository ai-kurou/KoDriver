package kurou.kodriver.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals

class GapTemplateFormatterTest {
    private val clause = "前方{ahead}秒"
    private val placeholder = "{ahead}"

    private fun String.replaceAhead(seconds: String?): String =
        replaceGap(
            clause = clause,
            placeholder = placeholder,
            seconds = seconds,
            noneClauseText = "前方車なし",
            noneValueText = "なし",
        )

    @Test
    fun `秒数が非nullならプレースホルダーを秒数に置換する`() {
        assertEquals("前方1.2秒", "前方{ahead}秒".replaceAhead("1.2"))
    }

    @Test
    fun `秒数がnullで節を含むときは節全体を車なしの文言に置換する`() {
        assertEquals("前方車なし", "前方{ahead}秒".replaceAhead(null))
    }

    @Test
    fun `秒数がnullで節を含まずプレースホルダーのみのときは車なしの値に置換する`() {
        assertEquals("差: なし", "差: {ahead}".replaceAhead(null))
    }

    @Test
    fun `節もプレースホルダーも含まないときは文言を変えない`() {
        assertEquals("注意", "注意".replaceAhead(null))
        assertEquals("注意", "注意".replaceAhead("1.2"))
    }
}
