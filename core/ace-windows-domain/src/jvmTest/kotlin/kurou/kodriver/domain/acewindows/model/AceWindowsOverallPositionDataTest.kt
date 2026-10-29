package kurou.kodriver.domain.acewindows.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsOverallPositionDataTest {
    @Test
    fun `総合順位と総台数をJSONへシリアライズして復元する`() {
        val data = AceWindowsOverallPositionData(position = 2, totalDrivers = 20)
        val json = """{"position":2,"totalDrivers":20}"""

        assertEquals(json, Json.encodeToString(data))
        assertEquals(data, Json.decodeFromString<AceWindowsOverallPositionData>(json))
    }

    @Test
    fun `未確定の0もJSONで保持する`() {
        val data = AceWindowsOverallPositionData(position = 0, totalDrivers = 0)

        assertEquals(data, Json.decodeFromString<AceWindowsOverallPositionData>(Json.encodeToString(data)))
    }
}
