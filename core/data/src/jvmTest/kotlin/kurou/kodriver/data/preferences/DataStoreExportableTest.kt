package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DataStoreExportableTest {
    private val tempDir = Files.createTempDirectory("kodriver_data_store_exportable_test").toFile()
    private val dataStore = createSoundVolumePreferencesDataStore(tempDir.absolutePath)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private fun exportable(sanitize: (SoundVolumePreferences) -> SoundVolumePreferences = { it }) =
        DataStoreExportable(
            key = "sound_volume",
            dataStore = dataStore,
            serializer = SoundVolumePreferences.serializer(),
            sanitize = sanitize,
        )

    @Test
    fun `keyは指定した値になる`() {
        assertEquals("sound_volume", exportable().key)
    }

    @Test
    fun `既定値のままでも全プロパティを書き出す`() =
        runTest {
            assertEquals(
                JsonObject(mapOf("volume" to JsonPrimitive(SoundVolumePreferences().volume))),
                exportable().export(),
            )
        }

    @Test
    fun `保存済みの値を書き出す`() =
        runTest {
            dataStore.updateData { it.copy(volume = 42) }

            assertEquals(JsonObject(mapOf("volume" to JsonPrimitive(42))), exportable().export())
        }

    @Test
    fun `書き出した値をインポートすると保存値に反映される`() =
        runTest {
            dataStore.updateData { it.copy(volume = 42) }
            val exported = exportable().export()
            dataStore.updateData { it.copy(volume = 7) }

            exportable().import(exported)

            assertEquals(42, dataStore.data.first().volume)
        }

    @Test
    fun `インポート時に未知のプロパティは無視する`() =
        runTest {
            exportable().import(JsonObject(mapOf("volume" to JsonPrimitive(30), "unknown" to JsonPrimitive("x"))))

            assertEquals(30, dataStore.data.first().volume)
        }

    @Test
    fun `インポート時にプロパティが欠けていれば既定値になる`() =
        runTest {
            dataStore.updateData { it.copy(volume = 7) }

            exportable().import(JsonObject(emptyMap()))

            assertEquals(SoundVolumePreferences().volume, dataStore.data.first().volume)
        }

    @Test
    fun `インポートした値は保存前にsanitizeで補正される`() =
        runTest {
            exportable(sanitize = { it.copy(volume = it.volume.coerceIn(0, 100)) })
                .import(JsonObject(mapOf("volume" to JsonPrimitive(500))))

            assertEquals(100, dataStore.data.first().volume)
        }

    @Test
    fun `型が合わない値のインポートは例外になり保存値は変わらない`() =
        runTest {
            dataStore.updateData { it.copy(volume = 7) }

            assertFailsWith<SerializationException> {
                exportable().import(JsonObject(mapOf("volume" to JsonPrimitive("loud"))))
            }
            assertFailsWith<IllegalArgumentException> { exportable().import(JsonArray(emptyList())) }

            assertEquals(7, dataStore.data.first().volume)
        }
}
