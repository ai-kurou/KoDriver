package kurou.kodriver.domain.gt7ps5.model

import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Gt7Ps5ManualReadoutPhrasesTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerGt7Ps5ReadoutItemKeys()
    }

    @Test
    fun `語句は手動読み上げ項目と同じキー構成で空語句や重複を含まない`() {
        assertEquals(gt7Ps5ManualReadoutItemDefinitions.keys, gt7Ps5ManualReadoutItemPhrases.keys)
        gt7Ps5ManualReadoutItemPhrases.values.forEach { phrases ->
            assertTrue(phrases.isNotEmpty())
            assertTrue(phrases.all { it.isNotBlank() })
            assertEquals(phrases.size, phrases.toSet().size)
        }
    }

    @Test
    fun `項目キーから語句を引け未知キーは空リスト`() {
        gt7Ps5ManualReadoutItemDefinitions.keys.forEach { key ->
            assertEquals(gt7Ps5ManualReadoutItemPhrases.getValue(key), gt7Ps5ManualReadoutItemPhrases(key))
        }
        assertEquals(emptyList(), gt7Ps5ManualReadoutItemPhrases("unknown"))
    }
}
