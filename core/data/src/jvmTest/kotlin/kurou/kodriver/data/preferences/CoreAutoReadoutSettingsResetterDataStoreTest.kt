package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.CoreAutoReadoutSettingsResetter
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CoreAutoReadoutSettingsResetterDataStoreTest {
    private val tempDir = Files.createTempDirectory("auto_readout_reset_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val autoReadoutDataStore =
        DataStoreFactory.create(
            serializer = AutoReadoutPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("autoReadout.pb") },
        )
    private val queueDataStore =
        DataStoreFactory.create(
            serializer = QueuePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("queue.pb") },
        )
    private val readoutStartSoundEnabledDataStore =
        DataStoreFactory.create(
            serializer = ReadoutStartSoundEnabledPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("readoutStartSoundEnabled.pb") },
        )
    private val resetter =
        CoreAutoReadoutSettingsResetter(
            autoReadoutPreferencesRepository = AutoReadoutPreferencesRepositoryImpl(autoReadoutDataStore),
            queuePreferencesRepository = QueuePreferencesRepositoryImpl(queueDataStore),
            readoutStartSoundEnabledPreferencesRepository =
                ReadoutStartSoundEnabledPreferencesRepositoryImpl(
                    readoutStartSoundEnabledDataStore,
                ),
        )

    @AfterTest
    fun tearDown() {
        dataStoreScope.cancel()
        tempDir.deleteRecursively()
    }

    @Test
    fun `全対象のDataStoreを既定値へ戻す`() =
        runTest {
            changeAutoReadoutSettings()
            changeQueueSettings()
            changeReadoutStartSoundEnabledSettings()

            assertNotEquals(AutoReadoutPreferencesSerializer.defaultValue, autoReadoutDataStore.data.first())
            assertNotEquals(QueuePreferencesSerializer.defaultValue, queueDataStore.data.first())
            assertNotEquals(
                ReadoutStartSoundEnabledPreferencesSerializer.defaultValue,
                readoutStartSoundEnabledDataStore.data.first(),
            )

            resetter.resetToDefault()

            assertEquals(AutoReadoutPreferencesSerializer.defaultValue, autoReadoutDataStore.data.first())
            assertEquals(QueuePreferencesSerializer.defaultValue, queueDataStore.data.first())
            assertEquals(
                ReadoutStartSoundEnabledPreferencesSerializer.defaultValue,
                readoutStartSoundEnabledDataStore.data.first(),
            )
        }

    private suspend fun changeAutoReadoutSettings() {
        autoReadoutDataStore.updateData {
            it.copy(
                simulatorStates =
                    listOf("lmu_windows", "ace_windows", "gt7_ps5").associateWith {
                        SimulatorAutoReadoutState(
                            enabledStates = mapOf("saved_key" to false),
                            itemOrder = listOf("saved_key"),
                        )
                    },
            )
        }
    }

    private suspend fun changeQueueSettings() {
        queueDataStore.updateData {
            it.copy(
                enabledStates = mapOf("saved_key" to false),
            )
        }
    }

    private suspend fun changeReadoutStartSoundEnabledSettings() {
        readoutStartSoundEnabledDataStore.updateData {
            it.copy(
                enabledStates = mapOf("saved_key" to false),
            )
        }
    }
}
