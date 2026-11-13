package kurou.kodriver.domain.acewindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsCarGapReadoutTextTest {
    @Test
    fun `既定文言は前後のプレースホルダーを含み未知のプレースホルダーを持たない`() {
        val template = ACE_WINDOWS_MANUAL_READOUT_CAR_GAP_TEXT_DEFAULT
        assertEquals("{ahead}、{behind}", template)
        assertEquals(template, ACE_WINDOWS_CAR_GAP_PLACEHOLDERS)
        assertEquals(emptyList(), findUnknownAceWindowsCarGapReadoutPlaceholders(template))
    }

    @Test
    fun `前後の秒数を小数1桁に丸めて置換する`() {
        assertEquals(
            "前方1.5秒、後方2.3秒",
            formatAceWindowsCarGapReadoutText("{ahead}、{behind}", AceWindowsGapData(1.5f, 2.26f)),
        )
        assertEquals(
            "前方12.0秒",
            formatAceWindowsCarGapReadoutText("{ahead}", AceWindowsGapData(11.96f, 0.5f)),
        )
    }

    @Test
    fun `負の値は絶対値で読む`() {
        assertEquals(
            "前方1.5秒、後方2.3秒",
            formatAceWindowsCarGapReadoutText("{ahead}、{behind}", AceWindowsGapData(-1.5f, -2.3f)),
        )
    }

    @Test
    fun `0と小数1桁で0になる値と非有限値は車なしにする`() {
        listOf(0f, -0f, 0.04f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY).forEach { value ->
            assertEquals(
                "前方車なし、後方車なし",
                formatAceWindowsCarGapReadoutText("{ahead}、{behind}", AceWindowsGapData(value, value)),
            )
        }
    }

    @Test
    fun `前後の片方だけがなしのときもう片方は秒数で読む`() {
        assertEquals(
            "前方車なし、後方3.0秒",
            formatAceWindowsCarGapReadoutText("{ahead}、{behind}", AceWindowsGapData(0f, 3f)),
        )
        assertEquals(
            "前方3.0秒、後方車なし",
            formatAceWindowsCarGapReadoutText("{ahead}、{behind}", AceWindowsGapData(3f, 0f)),
        )
    }

    @Test
    fun `前後以外のトークンは置換せず未知プレースホルダーとして重複なしで返す`() {
        assertEquals(
            "前方1.0秒 {other} {other} {seconds}",
            formatAceWindowsCarGapReadoutText("{ahead} {other} {other} {seconds}", AceWindowsGapData(1f, 2f)),
        )
        assertEquals(
            listOf("{other}", "{seconds}"),
            findUnknownAceWindowsCarGapReadoutPlaceholders("{ahead}{behind}{other}{other}{seconds}"),
        )
    }
}
