package kurou.kodriver.core.gt7ps5data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PreferencesDataStoreFactoryTest {
    @Serializable
    private data class FakePreferences(
        val name: String = "",
    )

    private val tempDir = Files.createTempDirectory("kodriver_preferences_data_store_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private fun createDataStore() =
        preferencesDataStore(
            directory = tempDir.absolutePath,
            fileName = FILE_NAME,
            serializer =
                protoBufPreferencesSerializer(
                    defaultValue = FakePreferences(),
                    kSerializer = FakePreferences.serializer(),
                ),
        )

    @Test
    fun `指定したファイル名でDataStoreが作成される`() =
        runTest {
            val dataStore = createDataStore()
            dataStore.updateData { it.copy(name = "saved") }

            assertTrue(tempDir.resolve(FILE_NAME).exists())
        }

    @Test
    fun `途中で切れた破損ファイルは既定値で読め、保存後の値を読み直せる`() =
        runTest {
            // 長さ5のlength-delimitedフィールドに対して1バイトしかない
            tempDir.resolve(FILE_NAME).writeBytes(byteArrayOf(0x0A, 0x05, 0x61))

            val dataStore = createDataStore()
            assertEquals(FakePreferences(), dataStore.data.first())

            dataStore.updateData { it.copy(name = "saved") }
            assertEquals(FakePreferences(name = "saved"), dataStore.data.first())
        }

    @Test
    fun `0埋めの破損ファイルは既定値で読め、保存後の値を読み直せる`() =
        runTest {
            tempDir.resolve(FILE_NAME).writeBytes(ByteArray(8))

            val dataStore = createDataStore()
            assertEquals(FakePreferences(), dataStore.data.first())

            dataStore.updateData { it.copy(name = "saved") }
            assertEquals(FakePreferences(name = "saved"), dataStore.data.first())
        }

    private companion object {
        const val FILE_NAME = "fake_preferences.pb"
    }
}
