package kurou.kodriver.core.designsystem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
    fun `小数第1位プレースホルダーに負の0点5境界値を埋め込むとString formatと同じhalf-upで丸められる`() {
        assertEquals("前後: -1.3 m", "前後: %1\$.1f m".formatSliderLabel(-1.25f))
    }

    @Test
    fun `小数第0位プレースホルダーに負の0点5境界値を埋め込むとString formatと同じhalf-upで丸められる`() {
        assertEquals("継続接近: -2 秒", "継続接近: %1\$.0f 秒".formatSliderLabel(-1.5f))
    }

    @Test
    fun `NaNとInfinityはKotlin標準のtoStringと同じ表記になる`() {
        assertEquals("値: NaN", "値: %1\$.1f".formatSliderLabel(Float.NaN))
        assertEquals("値: Infinity", "値: %1\$.1f".formatSliderLabel(Float.POSITIVE_INFINITY))
        assertEquals("値: -Infinity", "値: %1\$.1f".formatSliderLabel(Float.NEGATIVE_INFINITY))
    }

    @Test
    fun `文字列プレースホルダーとエスケープされたパーセント記号を含むテンプレートへ整数値を埋め込む`() {
        assertEquals("30%", "%1\$s%%".formatSliderLabel(30))
    }

    @Test
    fun `エスケープされたパーセント記号に続く文字列は未対応プレースホルダーとして扱わない`() {
        assertEquals("%2\$d", "%%2\$d".formatSliderLabel(3))
    }

    @Test
    fun `未対応の整数プレースホルダーを含むテンプレートは例外になる`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            "値: %2\$d".formatSliderLabel(3)
        }

        assertEquals("Unsupported slider label placeholder: %2\$d in template: 値: %2\$d", exception.message)
    }

    @Test
    fun `未対応の小数プレースホルダーを含むテンプレートは例外になる`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            "値: %1\$02.1f".formatSliderLabel(1.2f)
        }

        assertEquals("Unsupported slider label placeholder: %1\$02.1f in template: 値: %1\$02.1f", exception.message)
    }
}
