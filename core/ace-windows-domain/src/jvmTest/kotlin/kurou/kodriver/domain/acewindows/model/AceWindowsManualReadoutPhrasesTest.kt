package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.ReadoutItemKeyRegistry
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AceWindowsManualReadoutPhrasesTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerAceWindowsReadoutItemKeys()
    }

    @Test
    fun `語句は手動読み上げ項目と同じキー構成で空語句や重複を含まない`() {
        assertEquals(aceWindowsManualReadoutItemDefinitions.keys, aceWindowsManualReadoutItemPhrases.keys)
        aceWindowsManualReadoutItemPhrases.values.forEach { phrases ->
            assertTrue(phrases.isNotEmpty())
            assertTrue(phrases.all { it.isNotBlank() })
            assertEquals(phrases.size, phrases.toSet().size)
        }
    }

    @Test
    fun `同一の語句を複数の項目に持たせない`() {
        val allPhrases = aceWindowsManualReadoutItemPhrases.values.flatten()
        assertEquals(allPhrases.size, allPhrases.toSet().size)
    }

    @Test
    fun `項目キーから語句を引け未知キーは空リスト`() {
        aceWindowsManualReadoutItemDefinitions.keys.forEach { key ->
            assertEquals(aceWindowsManualReadoutItemPhrases.getValue(key), aceWindowsManualReadoutItemPhrases(key))
        }
        assertEquals(emptyList(), aceWindowsManualReadoutItemPhrases("unknown"))
    }

    @Test
    fun `登録したspecが語句を持ち音声タブの案内に使える`() {
        assertEquals(
            aceWindowsManualReadoutItemPhrases,
            ReadoutItemKeyRegistry.manualReadoutSpecOf(Simulator.AceWindows.id)?.itemPhrases,
        )
    }
}
