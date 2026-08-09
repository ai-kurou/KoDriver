package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.DebugStateCardKey
import kurou.kodriver.data.datasource.DebugStateCardOrderPreferencesSerializer
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DebugStateCardOrderPreferencesRepositoryImplTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_debug_state_card_order_preferences_test")
            .toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = DebugStateCardOrderPreferencesSerializer,
            scope = testScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = DebugStateCardOrderPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は空リスト・保存した順序を読み出せる・上書きで更新される`() =
        testScope.runTest {
            assertEquals(emptyList(), repository.observeCardOrder().first())

            repository.saveCardOrder(listOf(DebugStateCardKey.SESSION, DebugStateCardKey.SIMULATOR))
            assertEquals(
                listOf(DebugStateCardKey.SESSION, DebugStateCardKey.SIMULATOR),
                repository.observeCardOrder().first(),
            )

            repository.saveCardOrder(listOf(DebugStateCardKey.SIMULATOR, DebugStateCardKey.SESSION))
            assertEquals(
                listOf(DebugStateCardKey.SIMULATOR, DebugStateCardKey.SESSION),
                repository.observeCardOrder().first(),
            )
        }

    @Test
    fun `存在しないキー名は無視される`() =
        testScope.runTest {
            dataStore.updateData { it.copy(cardOrder = listOf("SESSION", "REMOVED_KEY", "SIMULATOR")) }

            assertEquals(
                listOf(DebugStateCardKey.SESSION, DebugStateCardKey.SIMULATOR),
                repository.observeCardOrder().first(),
            )
        }
}
