package kurou.kodriver.domain.gt7ps5.model

import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Gt7Ps5ManualReadoutPhraseMatcherTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerGt7Ps5ReadoutItemKeys()
    }

    @Test
    fun `語句に一致する発話は対応する項目キーを返す`() {
        assertEquals(
            Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value,
            matchGt7Ps5ManualReadoutItemKey(listOf("燃料残量を教えて")),
        )
        assertEquals(
            Gt7Ps5ReadoutItemKey.MyBestLap.Root.value,
            matchGt7Ps5ManualReadoutItemKey(listOf("自己ベスト")),
        )
        assertEquals(
            Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value,
            matchGt7Ps5ManualReadoutItemKey(listOf("タイヤ 温度")),
        )
    }

    @Test
    fun `燃料周回や助詞付きの周回数の質問は燃料残量より優先する`() {
        assertEquals(
            Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value,
            matchGt7Ps5ManualReadoutItemKey(listOf("燃料周回を教えて")),
        )
        assertEquals(
            Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value,
            matchGt7Ps5ManualReadoutItemKey(listOf("燃料はあと何周")),
        )
    }

    @Test
    fun `燃料の残量やガソリン残量は残量に一致し燃料や燃料は単独では一致しない`() {
        listOf("燃料の残量", "ガソリン残量").forEach { phrase ->
            assertEquals(
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value,
                matchGt7Ps5ManualReadoutItemKey(listOf(phrase)),
                phrase,
            )
        }
        listOf("燃料", "燃料は").forEach { phrase ->
            assertNull(matchGt7Ps5ManualReadoutItemKey(listOf(phrase)), phrase)
        }
    }

    @Test
    fun `一致しない・空の候補はnull`() {
        assertNull(matchGt7Ps5ManualReadoutItemKey(emptyList()))
        assertNull(matchGt7Ps5ManualReadoutItemKey(listOf("こんにちは")))
    }

    @Test
    fun `削除した語句は項目キーに一致せずnull`() {
        assertNull(matchGt7Ps5ManualReadoutItemKey(listOf("フューエル")))
        assertNull(matchGt7Ps5ManualReadoutItemKey(listOf("タイヤ熱")))
        assertNull(matchGt7Ps5ManualReadoutItemKey(listOf("タイヤテンプ")))
        assertNull(matchGt7Ps5ManualReadoutItemKey(listOf("じこべ")))
    }
}
