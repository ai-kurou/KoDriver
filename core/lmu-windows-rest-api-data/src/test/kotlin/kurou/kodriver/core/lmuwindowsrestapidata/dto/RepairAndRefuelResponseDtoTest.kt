package kurou.kodriver.core.lmuwindowsrestapidata.dto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RepairAndRefuelResponseDtoTest {
    @Test
    fun `JSONからwearablesとブレーキ厚さをデコードする`() {
        val json = """{"wearables":{"brakes":[0.036,0.035,0.032,0.031]}}"""

        val result = Json.decodeFromString<RepairAndRefuelResponseDto>(json)

        assertEquals(listOf(0.036, 0.035, 0.032, 0.031), result.wearables?.brakes)
    }

    @Test
    fun `wearablesとbrakesが省略された場合はnullになる`() {
        val resultWithoutWearables = Json.decodeFromString<RepairAndRefuelResponseDto>("{}")
        val resultWithoutBrakes = Json.decodeFromString<RepairAndRefuelResponseDto>("""{"wearables":{}}""")

        assertNull(resultWithoutWearables.wearables)
        assertEquals(WearablesDto(), resultWithoutBrakes.wearables)
        assertNull(resultWithoutBrakes.wearables?.brakes)
    }
}
