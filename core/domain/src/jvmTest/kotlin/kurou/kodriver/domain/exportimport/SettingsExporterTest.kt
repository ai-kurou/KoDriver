package kurou.kodriver.domain.exportimport

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SettingsExporterTest {
    private class FakeExportable(
        override val key: String,
        var value: JsonElement,
        private val importError: Exception? = null,
    ) : Exportable {
        override suspend fun export(): JsonElement = value

        override suspend fun import(value: JsonElement) {
            importError?.let { throw it }
            this.value = value
        }
    }

    @Test
    fun `exportはschemaVersionとappVersionと各キーの値を含むJSONを返す`() =
        runTest {
            val exporter =
                SettingsExporter(
                    exportables =
                        listOf(
                            FakeExportable("a", JsonPrimitive(1)),
                            FakeExportable("b", JsonPrimitive("x")),
                        ),
                    appVersion = "1.2.3",
                )

            val root = Json.parseToJsonElement(exporter.export()).jsonObject

            assertEquals(SettingsExporter.SCHEMA_VERSION, root.getValue("schemaVersion").jsonPrimitive.int)
            assertEquals("1.2.3", root.getValue("appVersion").jsonPrimitive.content)
            assertEquals(
                1,
                root
                    .getValue("settings")
                    .jsonObject
                    .getValue("a")
                    .jsonPrimitive.int,
            )
            assertEquals(
                "x",
                root
                    .getValue("settings")
                    .jsonObject
                    .getValue("b")
                    .jsonPrimitive.content,
            )
        }

    @Test
    fun `キーが重複するExportableは受け付けない`() {
        assertFailsWith<IllegalArgumentException> {
            SettingsExporter(
                exportables = listOf(FakeExportable("a", JsonPrimitive(1)), FakeExportable("a", JsonPrimitive(2))),
                appVersion = "1",
            )
        }
    }

    @Test
    fun `exportした内容をimportすると値が復元される`() =
        runTest {
            val source = FakeExportable("a", JsonPrimitive(5))
            val text = SettingsExporter(listOf(source), "1").export()
            val target = FakeExportable("a", JsonPrimitive(0))

            val result = SettingsExporter(listOf(target), "1").import(text)

            assertEquals(SettingsImportResult.Completed(listOf("a"), emptyList()), result)
            assertEquals(JsonPrimitive(5), target.value)
        }

    @Test
    fun `未知のキーは無視し欠けているキーは変更しない`() =
        runTest {
            val known = FakeExportable("a", JsonPrimitive(0))
            val missing = FakeExportable("b", JsonPrimitive(9))
            val text = """{"schemaVersion":1,"appVersion":"1","settings":{"a":3,"unknown":1}}"""

            val result = SettingsExporter(listOf(known, missing), "1").import(text)

            assertEquals(SettingsImportResult.Completed(listOf("a"), emptyList()), result)
            assertEquals(JsonPrimitive(3), known.value)
            assertEquals(JsonPrimitive(9), missing.value)
        }

    @Test
    fun `検証に失敗したキーはfailedKeysに入り他のキーは取り込まれる`() =
        runTest {
            val bad = FakeExportable("a", JsonPrimitive(0), importError = IllegalArgumentException("range"))
            val good = FakeExportable("b", JsonPrimitive(0))
            val text = """{"schemaVersion":1,"appVersion":"1","settings":{"a":3,"b":4}}"""

            val result = SettingsExporter(listOf(bad, good), "1").import(text)

            assertEquals(SettingsImportResult.Completed(listOf("b"), listOf("a")), result)
            assertEquals(JsonPrimitive(4), good.value)
        }

    @Test
    fun `キャンセルは握りつぶさず再スローする`() =
        runTest {
            val cancelling = FakeExportable("a", JsonPrimitive(0), importError = CancellationException("cancel"))
            val text = """{"schemaVersion":1,"appVersion":"1","settings":{"a":3}}"""

            assertFailsWith<CancellationException> { SettingsExporter(listOf(cancelling), "1").import(text) }
        }

    @Test
    fun `新しいschemaVersionはUnsupportedSchemaVersionを返し何も取り込まない`() =
        runTest {
            val target = FakeExportable("a", JsonPrimitive(0))
            val text = """{"schemaVersion":2,"appVersion":"9","settings":{"a":3}}"""

            val result = SettingsExporter(listOf(target), "1").import(text)

            assertEquals(SettingsImportResult.UnsupportedSchemaVersion(2), result)
            assertEquals(JsonPrimitive(0), target.value)
        }

    @Test
    fun `JSONとして読めない入力はInvalidFormatを返す`() =
        runTest {
            val exporter = SettingsExporter(emptyList(), "1")

            assertEquals(SettingsImportResult.InvalidFormat, exporter.import("not json"))
        }

    @Test
    fun `必須項目が欠けた入力はInvalidFormatを返す`() =
        runTest {
            val exporter = SettingsExporter(emptyList(), "1")

            assertEquals(SettingsImportResult.InvalidFormat, exporter.import("""{"settings":{}}"""))
        }

    @Test
    fun `JSONの最上位が配列の入力はInvalidFormatを返す`() =
        runTest {
            val exporter = SettingsExporter(emptyList(), "1")

            assertTrue(exporter.import("[]") is SettingsImportResult.InvalidFormat)
        }
}
