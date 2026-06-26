package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.Gt7Ps5RemainingFuelLapsPreferencesSerializer
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5RemainingFuelLapsPreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_gt7_remaining_fuel_laps_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val remainingFuelLapsDataStore = DataStoreFactory.create(
        serializer = Gt7Ps5RemainingFuelLapsPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("remaining_fuel_laps.pb") },
    )
    private val repository = Gt7Ps5RemainingFuelLapsPreferencesRepositoryImpl(
        remainingFuelLapsDataStore,
    )

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は3周`() = testScope.runTest {
        assertEquals(3, repository.observeRemainingFuelLaps().first())
    }

    @Test
    fun `保存した燃料残り周回数を取得できる`() = testScope.runTest {
        repository.saveRemainingFuelLaps(1)

        assertEquals(1, repository.observeRemainingFuelLaps().first())
    }

    @Test
    fun `燃料残り周回数を上書き保存できる`() = testScope.runTest {
        repository.saveRemainingFuelLaps(1)
        repository.saveRemainingFuelLaps(5)

        assertEquals(5, repository.observeRemainingFuelLaps().first())
    }
}
