package kurou.kodriver.core.gt7ps5data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5RemainingFuelPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_gt7_remaining_fuel_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = Gt7Ps5RemainingFuelPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("remaining_fuel.pb") },
        )
    private val repository = Gt7Ps5RemainingFuelPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は30パーセント`() =
        runTest {
            assertEquals(
                GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
                repository.observeThresholdPercentage().first(),
            )
        }

    @Test
    fun `保存した燃料残量閾値を取得できる`() =
        runTest {
            repository.saveThresholdPercentage(45)

            assertEquals(45, repository.observeThresholdPercentage().first())
        }

    @Test
    fun `燃料残量閾値を上書き保存できる`() =
        runTest {
            repository.saveThresholdPercentage(45)
            repository.saveThresholdPercentage(60)

            assertEquals(60, repository.observeThresholdPercentage().first())
        }

    @Test
    fun `文言の初期値は既定値で保存と空欄の上書きは閾値を維持する`() =
        runTest {
            assertEquals(GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT, repository.observeReadoutText().first())
            repository.saveThresholdPercentage(45)
            repository.saveReadoutText("残り{percent}%")
            assertEquals("残り{percent}%", repository.observeReadoutText().first())
            repository.saveReadoutText("")
            assertEquals("", repository.observeReadoutText().first())
            assertEquals(45, repository.observeThresholdPercentage().first())
            repository.saveThresholdPercentage(60)
            assertEquals("", repository.observeReadoutText().first())
        }

    @Test
    fun `保存した設定をすべて既定値に戻せる`() =
        runTest {
            val defaultThresholdPercentage = repository.observeThresholdPercentage().first()
            val defaultReadoutText = repository.observeReadoutText().first()

            repository.saveThresholdPercentage(7)
            repository.saveReadoutText("変更した文言")

            assertNotEquals(Gt7Ps5RemainingFuelPreferencesSerializer.defaultValue, dataStore.data.first())
            repository.resetToDefault()

            assertEquals(Gt7Ps5RemainingFuelPreferencesSerializer.defaultValue, dataStore.data.first())
            assertEquals(defaultThresholdPercentage, repository.observeThresholdPercentage().first())
            assertEquals(defaultReadoutText, repository.observeReadoutText().first())
        }
}
