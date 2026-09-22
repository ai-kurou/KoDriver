@file:Suppress("FunctionNaming")

package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsRemainingFuelPreferencesRepositoryImplTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_ace_windows_remaining_fuel_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = AceWindowsRemainingFuelPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = AceWindowsRemainingFuelPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `thresholdPercentage の初期値は 30`() =
        runTest {
            assertEquals(30, repository.observeThresholdPercentage().first())
        }

    @Test
    fun `saveThresholdPercentage で保存した値を observeThresholdPercentage で取得できる`() =
        runTest {
            repository.saveThresholdPercentage(50)
            assertEquals(50, repository.observeThresholdPercentage().first())
        }

    @Test
    fun `saveThresholdPercentage を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveThresholdPercentage(80)
            repository.saveThresholdPercentage(50)
            assertEquals(50, repository.observeThresholdPercentage().first())
        }
}
