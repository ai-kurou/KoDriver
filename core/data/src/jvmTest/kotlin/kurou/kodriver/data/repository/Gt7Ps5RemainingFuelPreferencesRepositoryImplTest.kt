package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.Gt7Ps5RemainingFuelPreferencesSerializer
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5RemainingFuelPreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_gt7_remaining_fuel_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
        serializer = Gt7Ps5RemainingFuelPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("remaining_fuel.pb") },
    )
    private val repository = Gt7Ps5RemainingFuelPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は30パーセント`() =
        testScope.runTest {
        assertEquals(
            GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
            repository.observeThresholdPercentage().first(),
        )
    }

    @Test
    fun `保存した燃料残量閾値を取得できる`() =
        testScope.runTest {
        repository.saveThresholdPercentage(45)

        assertEquals(45, repository.observeThresholdPercentage().first())
    }

    @Test
    fun `燃料残量閾値を上書き保存できる`() =
        testScope.runTest {
        repository.saveThresholdPercentage(45)
        repository.saveThresholdPercentage(60)

        assertEquals(60, repository.observeThresholdPercentage().first())
    }
}
