package kurou.kodriver.data.datasource

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutPreferencesDataStoreFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_readout_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @Test
    fun `readout_preferences_preferences_pbに書き込まれる`() = testScope.runTest {
        val dataStore = createReadoutPreferencesDataStore(tempDir.absolutePath)
        dataStore.edit { it[stringPreferencesKey("key")] = "value" }

        assertTrue(tempDir.resolve("readout_preferences.preferences_pb").exists())
    }
}
