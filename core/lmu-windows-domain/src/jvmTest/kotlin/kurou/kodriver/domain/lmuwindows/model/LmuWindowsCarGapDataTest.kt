package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsCarGapDataTest {
    @Test
    fun `JSONへシリアライズして復元できる`() {
        val data = LmuWindowsCarGapData(1.0, 4.1, 2.0, 5.5)

        val restored = Json.decodeFromString<LmuWindowsCarGapData>(Json.encodeToString(data))

        assertEquals(data, restored)
    }
}
