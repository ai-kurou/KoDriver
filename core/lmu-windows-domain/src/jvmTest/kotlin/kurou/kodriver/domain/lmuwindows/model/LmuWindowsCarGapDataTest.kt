package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsCarGapDataTest {
    @Test
    fun `JSONへシリアライズして復元できる`() {
        val data = LmuWindowsCarGapData(1.0, 4.1, 2.0, 5.5, 1.25, 9.5)

        val restored = Json.decodeFromString<LmuWindowsCarGapData>(Json.encodeToString(data))

        assertEquals(data, restored)
    }

    @Test
    fun `全体ギャップがnullでもJSON往復できる`() {
        val data = LmuWindowsCarGapData(1.0, 4.1, 2.0, 5.5)

        val json = Json { encodeDefaults = true }

        assertEquals(data, json.decodeFromString<LmuWindowsCarGapData>(json.encodeToString(data)))
    }

    @Test
    fun `旧JSONは全体ギャップをnullとして復元する`() {
        val json =
            """{"timeGapCarAheadSeconds":1.0,"timeGapCarBehindSeconds":4.1,""" +
                """"timeGapPlaceAheadSeconds":2.0,"timeGapPlaceBehindSeconds":5.5}"""

        assertEquals(LmuWindowsCarGapData(1.0, 4.1, 2.0, 5.5), Json.decodeFromString<LmuWindowsCarGapData>(json))
    }
}
