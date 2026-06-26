package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5RemainingFuelLapsPreferencesRepositoryFactoryTest {

    private val tempDir = Files.createTempDirectory(
        "kodriver_gt7_remaining_fuel_laps_preferences_repository_factory_test",
    )
        .toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は3周`() = testScope.runTest {
        val repository = createGt7Ps5RemainingFuelLapsPreferencesRepository(
            directory = tempDir.absolutePath,
        )

        assertEquals(3, repository.observeRemainingFuelLaps().first())
    }

    @Test
    fun `保存した燃料残り周回数を読み出せる`() = testScope.runTest {
        val repository = createGt7Ps5RemainingFuelLapsPreferencesRepository(
            directory = tempDir.absolutePath,
        )

        repository.saveRemainingFuelLaps(5)

        assertEquals(5, repository.observeRemainingFuelLaps().first())
    }
}
