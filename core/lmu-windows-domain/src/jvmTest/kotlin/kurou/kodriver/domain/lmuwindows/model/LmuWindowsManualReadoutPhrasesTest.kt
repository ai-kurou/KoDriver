package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKeyRegistry
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsManualReadoutPhrasesTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerLmuWindowsReadoutItemKeys()
    }

    @Test
    fun `語句は手動読み上げ項目と同じキー構成で空語句や重複を含まない`() {
        assertEquals(lmuWindowsManualReadoutItemDefinitions.keys, lmuWindowsManualReadoutItemPhrases.keys)
        lmuWindowsManualReadoutItemPhrases.values.forEach { phrases ->
            assertTrue(phrases.isNotEmpty())
            assertTrue(phrases.all { it.isNotBlank() })
            assertEquals(phrases.size, phrases.toSet().size)
        }
    }

    @Test
    fun `同一の語句を複数の項目に持たせない`() {
        val allPhrases = lmuWindowsManualReadoutItemPhrases.values.flatten()
        assertEquals(allPhrases.size, allPhrases.toSet().size)
    }

    @Test
    fun `項目キーから語句を引け未知キーは空リスト`() {
        lmuWindowsManualReadoutItemDefinitions.keys.forEach { key ->
            assertEquals(lmuWindowsManualReadoutItemPhrases.getValue(key), lmuWindowsManualReadoutItemPhrases(key))
        }
        assertEquals(emptyList(), lmuWindowsManualReadoutItemPhrases("unknown"))
    }

    @Test
    fun `登録したspecが語句を持ち音声タブの案内に使える`() {
        assertEquals(
            lmuWindowsManualReadoutItemPhrases,
            ReadoutItemKeyRegistry.manualReadoutSpecOf(Simulator.LmuWindows.id)?.itemPhrases,
        )
    }

    @Test
    fun `前後車とのギャップは短く誤認識しやすい前方と後方を語句に含めない`() {
        val ahead = lmuWindowsManualReadoutItemPhrases(LmuWindowsReadoutItemKey.ClassCarGap.Ahead.value)
        val behind = lmuWindowsManualReadoutItemPhrases(LmuWindowsReadoutItemKey.ClassCarGap.Behind.value)
        assertTrue("前方" !in ahead)
        assertTrue("後方" !in behind)
        assertTrue("前との差" in ahead)
        assertTrue("後ろとの差" in behind)
    }
}
