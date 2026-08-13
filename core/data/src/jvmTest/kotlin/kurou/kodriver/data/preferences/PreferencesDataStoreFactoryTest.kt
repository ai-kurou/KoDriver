package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class PreferencesDataStoreFactoryTest {
    @Serializable
    private data class FakePreferences(
        val value: Int = 0,
    )

    private val tempDir = Files.createTempDirectory("kodriver_preferences_data_store_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `指定したファイル名でDataStoreが作成される`() =
        runTest {
            val serializer =
                protoBufPreferencesSerializer(
                    defaultValue = FakePreferences(),
                    kSerializer = FakePreferences.serializer(),
                )

            val dataStore =
                preferencesDataStore(
                    directory = tempDir.absolutePath,
                    fileName = "fake_preferences.pb",
                    serializer = serializer,
                )
            dataStore.updateData { it.copy(value = 1) }

            assertTrue(tempDir.resolve("fake_preferences.pb").exists())
        }
}
