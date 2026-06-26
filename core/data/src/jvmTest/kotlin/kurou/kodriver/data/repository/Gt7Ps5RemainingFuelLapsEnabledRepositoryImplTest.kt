package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.ReadoutPreferencesSerializer
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5RemainingFuelLapsEnabledRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_gt7_remaining_fuel_laps_enabled_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = ReadoutPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = Gt7Ps5RemainingFuelLapsEnabledRepositoryImpl(
        ReadoutPreferencesRepositoryImpl(dataStore),
    )

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は true`() = testScope.runTest {
        assertTrue(repository.observeEnabled().first())
    }

    @Test
    fun `保存した有効状態を取得できる`() = testScope.runTest {
        repository.saveEnabled(false)

        assertFalse(repository.observeEnabled().first())
    }

    @Test
    fun `有効状態を上書き保存できる`() = testScope.runTest {
        repository.saveEnabled(false)
        repository.saveEnabled(true)

        assertTrue(repository.observeEnabled().first())
    }
}
