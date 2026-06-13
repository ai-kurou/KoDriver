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
class VehicleApproachPreferencesRepositoryFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_vehicle_approach_preferences_repository_factory_test")
        .toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は skipFirstLap が true`() = testScope.runTest {
        val repository = createVehicleApproachPreferencesRepository(tempDir.absolutePath)

        assertEquals(true, repository.observeSkipFirstLap().first())
    }

    @Test
    fun `保存した skipFirstLap を読み出せる`() = testScope.runTest {
        val repository = createVehicleApproachPreferencesRepository(tempDir.absolutePath)

        repository.saveSkipFirstLap(true)

        assertEquals(true, repository.observeSkipFirstLap().first())
    }
}
