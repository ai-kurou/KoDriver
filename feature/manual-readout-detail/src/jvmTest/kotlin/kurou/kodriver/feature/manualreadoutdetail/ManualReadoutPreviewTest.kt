package kurou.kodriver.feature.manualreadoutdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.manualReadoutItemKeys
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutPreviewTest {
    @Test
    fun `タイヤ摩耗の既定文言と編集文言はサンプル値に置換する`() {
        val key = manualReadoutItemKeys(Simulator.LmuWindows.id).last()
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("タイヤ摩耗残り50%", manualReadoutPreview(state))
        assertEquals("残り50%", manualReadoutPreview(state.copy(itemTexts = mapOf(key to "残り{percent}%"))))
    }

    @Test
    fun `未選択と空欄はプレビューに含めない`() {
        assertEquals("", manualReadoutPreview(ManualReadoutSetDetailUiState()))
        val key = manualReadoutItemKeys(Simulator.LmuWindows.id).first()
        assertEquals(
            "",
            manualReadoutPreview(
                ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key), itemTexts = mapOf(key to "  ")),
            ),
        )
    }

    @Test
    fun `既定文言と編集文言の残量はサンプル値に置換し未知トークンは保持する`() {
        val key = manualReadoutItemKeys(Simulator.LmuWindows.id).first()
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("バーチャルエナジー残量は50パーセント", manualReadoutPreview(state))
        assertEquals(
            "残り50% {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "残り{percent}% {unknown}"))),
        )
    }

    @Test
    fun `保存済みの選択順に空欄以外を連結する`() {
        assertEquals(
            "後、先",
            manualReadoutPreview(
                ManualReadoutSetDetailUiState(
                    selectedItemKeys = listOf("second", "blank", "first", "missing"),
                    itemTexts = mapOf("first" to "先", "second" to "後", "blank" to ""),
                ),
            ),
        )
    }
}
