package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AceWindowsManualReadoutPhraseMatcherTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerAceWindowsReadoutItemKeys()
    }

    @Test
    fun `語句に一致する発話は対応する項目キーを返す`() {
        assertEquals(
            AceWindowsReadoutItemKey.RemainingFuel.Root.value,
            matchAceWindowsManualReadoutItemKey(listOf("燃料残量を教えて")),
        )
        assertEquals(
            AceWindowsReadoutItemKey.MyBestLap.Root.value,
            matchAceWindowsManualReadoutItemKey(listOf("自己ベスト")),
        )
        assertEquals(
            AceWindowsReadoutItemKey.TyreTemperature.Root.value,
            matchAceWindowsManualReadoutItemKey(listOf("タイヤ 温度")),
        )
    }

    @Test
    fun `燃料周回や助詞付きの周回数の質問は燃料残量より優先する`() {
        assertEquals(
            AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value,
            matchAceWindowsManualReadoutItemKey(listOf("燃料周回を教えて")),
        )
        assertEquals(
            AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value,
            matchAceWindowsManualReadoutItemKey(listOf("燃料はあと何周")),
        )
    }

    @Test
    fun `削除した語句は一致せずnull`() {
        assertNull(matchAceWindowsManualReadoutItemKey(listOf("フューエル")))
        assertNull(matchAceWindowsManualReadoutItemKey(listOf("タイヤテンプ")))
        assertNull(matchAceWindowsManualReadoutItemKey(listOf("じこべ")))
    }

    @Test
    fun `燃料の残量やガソリン残量は残量に一致し燃料や燃料は単独では一致しない`() {
        listOf("燃料の残量", "ガソリン残量").forEach { phrase ->
            assertEquals(
                AceWindowsReadoutItemKey.RemainingFuel.Root.value,
                matchAceWindowsManualReadoutItemKey(listOf(phrase)),
                phrase,
            )
        }
        listOf("燃料", "燃料は").forEach { phrase ->
            assertNull(matchAceWindowsManualReadoutItemKey(listOf(phrase)), phrase)
        }
    }

    @Test
    fun `一致しない・空の候補はnull`() {
        assertNull(matchAceWindowsManualReadoutItemKey(emptyList()))
        assertNull(matchAceWindowsManualReadoutItemKey(listOf("こんにちは")))
    }

    @Test
    fun `一致した語句と項目キーを返し一致がなければnullを返す`() {
        val match = matchAceWindowsManualReadoutPhrase(listOf("自己ベストを教えて"))
        assertEquals(AceWindowsReadoutItemKey.MyBestLap.Root.value, match?.itemKey)
        assertEquals("自己ベスト", match?.phrase)
        assertNull(matchAceWindowsManualReadoutPhrase(listOf("関係ない発話")))
    }

    private fun matchAceWindowsManualReadoutItemKey(candidates: List<String>): String? =
        matchAceWindowsManualReadoutPhrase(candidates)?.itemKey
}
