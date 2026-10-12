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
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DebugStateCardOrderPreferencesRepositoryImplTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_debug_state_card_order_preferences_test")
            .toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = DebugStateCardOrderPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = DebugStateCardOrderPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `未知名を含む文字列の順序をそのまま保存して読み出せる`() =
        runTest {
            val order = listOf("BRAKE_WEAR", "BEST_LAP", "REMOVED_KEY", "SIMULATOR")
            repository.saveCardOrder(order)

            assertEquals(order, repository.observeCardOrder().first())
        }

    @Test
    fun `初期値は空リスト・保存した順序を読み出せる・上書きで更新される`() =
        runTest {
            assertEquals(emptyList(), repository.observeCardOrder().first())

            repository.saveCardOrder(listOf("SESSION", "SIMULATOR"))
            assertEquals(
                listOf("SESSION", "SIMULATOR"),
                repository.observeCardOrder().first(),
            )

            repository.saveCardOrder(listOf("SIMULATOR", "SESSION"))
            assertEquals(
                listOf("SIMULATOR", "SESSION"),
                repository.observeCardOrder().first(),
            )
        }

    @Test
    fun `旧データの未知名もそのまま読み出す`() =
        runTest {
            dataStore.updateData { it.copy(cardOrder = listOf("SESSION", "REMOVED_KEY", "SIMULATOR")) }

            assertEquals(
                listOf("SESSION", "REMOVED_KEY", "SIMULATOR"),
                repository.observeCardOrder().first(),
            )
        }
}
