package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.CoreManualReadoutSettingsResetter
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CoreManualReadoutSettingsResetterDataStoreTest {
    private val tempDir = Files.createTempDirectory("manual_readout_reset_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val lmuDataStore = createSetDataStore("lmu.pb")
    private val aceDataStore = createSetDataStore("ace.pb")
    private val gt7DataStore = createSetDataStore("gt7.pb")
    private val playButtonDataStore =
        DataStoreFactory.create(
            serializer = ManualReadoutPlayButtonLayoutPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("playButton.pb") },
        )
    private val resetter =
        CoreManualReadoutSettingsResetter(
            lmuWindowsManualReadoutSetPreferencesRepository = ManualReadoutSetPreferencesRepositoryImpl(lmuDataStore),
            aceWindowsManualReadoutSetPreferencesRepository = ManualReadoutSetPreferencesRepositoryImpl(aceDataStore),
            gt7Ps5ManualReadoutSetPreferencesRepository = ManualReadoutSetPreferencesRepositoryImpl(gt7DataStore),
            manualReadoutPlayButtonLayoutEnabledPreferencesRepository =
                ManualReadoutPlayButtonLayoutPreferencesRepositoryImpl(playButtonDataStore),
        )

    @AfterTest
    fun tearDown() {
        dataStoreScope.cancel()
        tempDir.deleteRecursively()
    }

    @Test
    fun `全対象のDataStoreを既定値へ戻す`() =
        runTest {
            val sets = listOf(ManualReadoutSetPreference("set-1"))
            listOf(lmuDataStore, aceDataStore, gt7DataStore).forEach { store ->
                store.updateData { it.copy(sets = sets) }
            }
            playButtonDataStore.updateData { it.copy(enabled = !it.enabled) }

            listOf(lmuDataStore, aceDataStore, gt7DataStore).forEach {
                assertNotEquals(ManualReadoutSetPreferencesSerializer.defaultValue, it.data.first())
            }
            val playButtonDefault = ManualReadoutPlayButtonLayoutPreferencesSerializer.defaultValue
            assertNotEquals(playButtonDefault, playButtonDataStore.data.first())

            resetter.resetToDefault()

            listOf(lmuDataStore, aceDataStore, gt7DataStore).forEach {
                assertEquals(ManualReadoutSetPreferencesSerializer.defaultValue, it.data.first())
            }
            assertEquals(playButtonDefault, playButtonDataStore.data.first())
        }

    private fun createSetDataStore(fileName: String) =
        DataStoreFactory.create(
            serializer = ManualReadoutSetPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve(fileName) },
        )
}
