package kurou.kodriver.domain.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutItemKeyMapSerializerTest {
    private object DummyKey : ReadoutItemKey {
        override val value: String = "dummy"
    }

    private val serializer = object : ReadoutItemKeyMapSerializer({ listOf(DummyKey) }) {}

    @Test
    fun `シリアライズ時にReadoutItemKeyをvalueへ変換する`() {
        val map = mapOf<ReadoutItemKey, Boolean>(DummyKey to true)

        val json = Json.encodeToString(serializer, map)

        val root = Json.parseToJsonElement(json).jsonObject
        assertEquals(true, root["dummy"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun `デシリアライズ時にvalueからReadoutItemKeyへ復元する`() {
        val json = """{"dummy":true}"""

        val map: Map<ReadoutItemKey, Boolean> = Json.decodeFromString(serializer, json)

        assertEquals(mapOf<ReadoutItemKey, Boolean>(DummyKey to true), map)
    }

    @Test
    fun `デシリアライズ時に不明なvalueは無視する`() {
        val json = """{"unknown_key":true,"dummy":false}"""

        val map: Map<ReadoutItemKey, Boolean> = Json.decodeFromString(serializer, json)

        assertEquals(mapOf<ReadoutItemKey, Boolean>(DummyKey to false), map)
    }
}
