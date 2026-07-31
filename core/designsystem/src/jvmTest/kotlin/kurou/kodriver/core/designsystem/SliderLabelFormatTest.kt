package kurou.kodriver.core.designsystem

import kotlin.test.Test
import kotlin.test.assertEquals

class SliderLabelFormatTest {

    @Test
    fun `整数プレースホルダーを含むテンプレートへ整数値を埋め込む`() {
        assertEquals("残り約: 3 周", "残り約: %1\$d 周".formatSliderLabel(3))
    }

    @Test
    fun `小数第1位プレースホルダーを含むテンプレートへ小数値を埋め込む`() {
        assertEquals("前後: 1.2 m", "前後: %1\$.1f m".formatSliderLabel(1.2f))
    }

    @Test
    fun `小数第1位プレースホルダーに負の値を埋め込む`() {
        assertEquals("前後: -1.2 m", "前後: %1\$.1f m".formatSliderLabel(-1.2f))
    }

    @Test
    fun `小数第0位プレースホルダーは整数として丸めて埋め込む`() {
        assertEquals("継続接近: 3 秒", "継続接近: %1\$.0f 秒".formatSliderLabel(3.4f))
    }

    @Test
    fun `文字列プレースホルダーとエスケープされたパーセント記号を含むテンプレートへ整数値を埋め込む`() {
        assertEquals("30%", "%1\$s%%".formatSliderLabel(30))
    }
}
