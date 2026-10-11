package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Simulator
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DebugStateCardOrderPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_debug_state_card_order_preferences_repository_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は空リスト`() =
        runTest {
            val repository = createDebugStateCardOrderPreferencesRepository(tempDir.absolutePath, Simulator.LmuWindows)

            assertEquals(emptyList(), repository.observeCardOrder().first())
        }

    @Test
    fun `保存した順序を読み出せる`() =
        runTest {
            val repository = createDebugStateCardOrderPreferencesRepository(tempDir.absolutePath, Simulator.LmuWindows)

            repository.saveCardOrder(listOf(DebugStateCardKey.SESSION, DebugStateCardKey.SIMULATOR))

            assertEquals(
                listOf(DebugStateCardKey.SESSION, DebugStateCardKey.SIMULATOR),
                repository.observeCardOrder().first(),
            )
        }

    @Test
    fun `シミュレーターごとに独立して順序を保存する`() =
        runTest {
            val lmuRepository =
                createDebugStateCardOrderPreferencesRepository(tempDir.absolutePath, Simulator.LmuWindows)
            val aceRepository =
                createDebugStateCardOrderPreferencesRepository(tempDir.absolutePath, Simulator.AceWindows)

            lmuRepository.saveCardOrder(listOf(DebugStateCardKey.SESSION, DebugStateCardKey.SIMULATOR))
            assertEquals(emptyList(), aceRepository.observeCardOrder().first())

            aceRepository.saveCardOrder(listOf(DebugStateCardKey.BEST_LAP, DebugStateCardKey.SIMULATOR))

            assertEquals(
                listOf(DebugStateCardKey.SESSION, DebugStateCardKey.SIMULATOR),
                lmuRepository.observeCardOrder().first(),
            )
            assertEquals(
                listOf(DebugStateCardKey.BEST_LAP, DebugStateCardKey.SIMULATOR),
                aceRepository.observeCardOrder().first(),
            )
        }
}
