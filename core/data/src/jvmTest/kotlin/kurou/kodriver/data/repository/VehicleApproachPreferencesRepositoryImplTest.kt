package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.VehicleApproachPreferencesSerializer
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
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

    @Test
    fun `startReadoutEnabled の初期値は true`() = testScope.runTest {
        assertEquals(true, repository.observeStartReadoutEnabled().first())
    }

    @Test
    fun `saveStartReadoutEnabled で保存した値を observeStartReadoutEnabled で取得できる`() = testScope.runTest {
        repository.saveStartReadoutEnabled(false)
        assertEquals(false, repository.observeStartReadoutEnabled().first())
    }

    @Test
    fun `saveStartReadoutEnabled を複数回呼ぶと最後の値で上書きされる`() = testScope.runTest {
        repository.saveStartReadoutEnabled(false)
        repository.saveStartReadoutEnabled(true)
        assertEquals(true, repository.observeStartReadoutEnabled().first())
    }

    @Test
    fun `startReadoutType の初期値は CAR_LEFT_RIGHT`() = testScope.runTest {
        assertEquals(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT, repository.observeStartReadoutType().first())
    }

    @Test
    fun `saveStartReadoutType で保存した値を observeStartReadoutType で取得できる`() = testScope.runTest {
        repository.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)
        assertEquals(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH, repository.observeStartReadoutType().first())
    }

    @Test
    fun `saveStartReadoutType を複数回呼ぶと最後の値で上書きされる`() = testScope.runTest {
        repository.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)
        repository.saveStartReadoutType(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
        assertEquals(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT, repository.observeStartReadoutType().first())
    }

    @Test
    fun `startReadoutType が未知の ID のとき CAR_LEFT_RIGHT を返す`() = testScope.runTest {
        dataStore.updateData { it.copy(startReadoutType = "unknown") }

        assertEquals(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT, repository.observeStartReadoutType().first())
    }
}
