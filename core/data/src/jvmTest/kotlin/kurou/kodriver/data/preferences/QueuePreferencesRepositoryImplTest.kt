package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QueuePreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_queue_prefs_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = QueuePreferencesSerializer,
            scope = testScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = QueuePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は空Map・保存した値を返す・上書きで更新される`() =
        testScope.runTest {
            assertTrue(repository.observeQueueEnabledStates().first().isEmpty())

            repository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, true)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.Root to true),
                repository.observeQueueEnabledStates().first(),
            )

            repository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, false)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.Root to false),
                repository.observeQueueEnabledStates().first(),
            )
        }

    @Test
    fun `複数項目を独立して保存・取得できる`() =
        testScope.runTest {
            repository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, true)
            repository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.Root, false)
            repository.saveQueueEnabledState(ReadoutItemKey.Gt7Ps5.MyBestLap.Root, true)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.Flag.Root to true,
                    ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.Root to true,
                ),
                repository.observeQueueEnabledStates().first(),
            )
        }
}
