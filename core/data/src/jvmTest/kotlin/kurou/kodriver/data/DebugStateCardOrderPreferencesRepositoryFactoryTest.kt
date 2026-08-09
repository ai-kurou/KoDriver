package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.DebugStateCardKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DebugStateCardOrderPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_debug_state_card_order_preferences_repository_factory_test")
            .toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は空リスト`() =
        testScope.runTest {
            val repository = createDebugStateCardOrderPreferencesRepository(tempDir.absolutePath)

            assertEquals(emptyList(), repository.observeCardOrder().first())
        }

    @Test
    fun `保存した順序を読み出せる`() =
        testScope.runTest {
            val repository = createDebugStateCardOrderPreferencesRepository(tempDir.absolutePath)

            repository.saveCardOrder(listOf(DebugStateCardKey.SESSION, DebugStateCardKey.SIMULATOR))

            assertEquals(
                listOf(DebugStateCardKey.SESSION, DebugStateCardKey.SIMULATOR),
                repository.observeCardOrder().first(),
            )
        }
}
