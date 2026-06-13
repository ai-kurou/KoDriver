package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.VehicleApproachPreferencesSerializer
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleApproachPreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_vehicle_approach_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = VehicleApproachPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = VehicleApproachPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `skipFirstLap の初期値は true`() = testScope.runTest {
        assertEquals(true, repository.observeSkipFirstLap().first())
    }

    @Test
    fun `saveSkipFirstLap で保存した値を observeSkipFirstLap で取得できる`() = testScope.runTest {
        repository.saveSkipFirstLap(true)
        assertEquals(true, repository.observeSkipFirstLap().first())
    }

    @Test
    fun `saveSkipFirstLap を複数回呼ぶと最後の値で上書きされる`() = testScope.runTest {
        repository.saveSkipFirstLap(true)
        repository.saveSkipFirstLap(false)
        assertEquals(false, repository.observeSkipFirstLap().first())
    }
}
