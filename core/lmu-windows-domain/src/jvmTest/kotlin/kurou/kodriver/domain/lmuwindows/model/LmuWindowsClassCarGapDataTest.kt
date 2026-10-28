package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsClassCarGapDataTest {
    @Test
    fun `JSONへシリアライズして復元できる`() {
        val data = LmuWindowsClassCarGapData(1.0, 4.1)

        val restored = Json.decodeFromString<LmuWindowsClassCarGapData>(Json.encodeToString(data))

        assertEquals(data, restored)
    }

    @Test
    fun `nullをシリアライズして復元できる`() {
        val data = LmuWindowsClassCarGapData(null, null)

        val restored = Json.decodeFromString<LmuWindowsClassCarGapData>(Json.encodeToString(data))

        assertEquals(data, restored)
        assertEquals(LmuWindowsClassCarGapData(1.0, 2.0), data.copy(gapAheadSeconds = 1.0, gapBehindSeconds = 2.0))
    }
}
