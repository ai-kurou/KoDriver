package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QueuePreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_queue_prefs_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = QueuePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = QueuePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は空Map・保存した値を返す・上書きで更新される`() =
        runTest {
            assertTrue(repository.observeQueueEnabledStates().first().isEmpty())

            repository.saveQueueEnabledState(LmuWindowsReadoutItemKey.Flag.Root, true)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.Flag.Root to true),
                repository.observeQueueEnabledStates().first(),
            )

            repository.saveQueueEnabledState(LmuWindowsReadoutItemKey.Flag.Root, false)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.Flag.Root to false),
                repository.observeQueueEnabledStates().first(),
            )
        }

    @Test
    fun `複数項目を独立して保存・取得できる`() =
        runTest {
            repository.saveQueueEnabledState(LmuWindowsReadoutItemKey.Flag.Root, true)
            repository.saveQueueEnabledState(LmuWindowsReadoutItemKey.TyreTemperature.Root, false)
            repository.saveQueueEnabledState(Gt7Ps5ReadoutItemKey.MyBestLap.Root, true)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.Flag.Root to true,
                    LmuWindowsReadoutItemKey.TyreTemperature.Root to false,
                    Gt7Ps5ReadoutItemKey.MyBestLap.Root to true,
                ),
                repository.observeQueueEnabledStates().first(),
            )
        }
}
