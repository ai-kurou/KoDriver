package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutTextTest {
    @Test
    fun `項目文言がなければ対象項目なしの文言を返す`() {
        assertEquals(MANUAL_READOUT_NO_ITEMS_TEXT, joinManualReadoutTexts(emptyList()))
    }

    @Test
    fun `項目文言を読点で連結する`() {
        assertEquals("A、B、C", joinManualReadoutTexts(listOf("A", "B", "C")))
    }

    @Test
    fun `空文言の項目は連結から除外する`() {
        assertEquals("A、C", joinManualReadoutTexts(listOf("A", "", " ", "C")))
    }

    @Test
    fun `全項目が空文言なら空文字を返す`() {
        assertEquals("", joinManualReadoutTexts(listOf("", " ")))
    }

    @Test
    fun `データなし文言は項目名とデータがありませんを連結する`() {
        assertEquals("残り燃料はデータがありません", manualReadoutNoDataText("残り燃料"))
        assertEquals("データがありません", MANUAL_READOUT_NO_DATA_TEXT)
    }
}
