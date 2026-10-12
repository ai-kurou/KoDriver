package kurou.kodriver.domain.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LmuWindowsReadoutItemKeyMapSerializerTest {
    @Test
    fun `シリアライズ時にReadoutItemKeyをvalueへ変換する`() {
        val map = mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.Flag.Root to true)

        val json = Json.encodeToString(LmuWindowsReadoutItemKeyMapSerializer, map)

        val root = Json.parseToJsonElement(json).jsonObject
        assertEquals(true, root["lmu_windows_flag"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun `デシリアライズ時にvalueからReadoutItemKeyへ復元する`() {
        val json = """{"lmu_windows_flag":true}"""

        val map: Map<ReadoutItemKey, Boolean> = Json.decodeFromString(LmuWindowsReadoutItemKeyMapSerializer, json)

        assertEquals(mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.Flag.Root to true), map)
    }

    @Test
    fun `デシリアライズ時に不明なvalueは無視する`() {
        val json = """{"unknown_key":true,"gt7_ps5_remaining_fuel":false}"""

        val map: Map<ReadoutItemKey, Boolean> = Json.decodeFromString(LmuWindowsReadoutItemKeyMapSerializer, json)

        assertNull(map[LmuWindowsReadoutItemKey.Flag.Root])
        assertEquals(emptyMap(), map)
    }
}
