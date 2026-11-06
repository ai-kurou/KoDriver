package kurou.kodriver.domain.lmuwindows.model

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
    fun `項目キーから語句を引け未知キーは空リスト`() {
        lmuWindowsManualReadoutItemDefinitions.keys.forEach { key ->
            assertEquals(lmuWindowsManualReadoutItemPhrases.getValue(key), lmuWindowsManualReadoutItemPhrases(key))
        }
        assertEquals(emptyList(), lmuWindowsManualReadoutItemPhrases("unknown"))
    }
}
