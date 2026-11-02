package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class ManualReadoutPlayButtonLayoutPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_manual_readout_play_button_layout_repo_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = ManualReadoutPlayButtonLayoutPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = ManualReadoutPlayButtonLayoutPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `resetToDefaultで既定値へ戻す`() =
        runTest {
            repository.saveManualReadoutPlayButtonLayoutEnabled(true)

            repository.resetToDefault()

            assertFalse(repository.observeManualReadoutPlayButtonLayoutEnabled().first())
        }
}
