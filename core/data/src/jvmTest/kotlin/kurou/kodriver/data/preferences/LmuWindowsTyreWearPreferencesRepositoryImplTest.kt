package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsTyreWearPreferencesRepositoryImplTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_tyre_wear_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsTyreWearPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsTyreWearPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `thresholdPercentage の初期値は 50`() =
        runTest {
            assertEquals(50, repository.observeThresholdPercentage().first())
        }

    @Test
    fun `saveThresholdPercentage で保存した値を observeThresholdPercentage で取得できる`() =
        runTest {
            repository.saveThresholdPercentage(30)
            assertEquals(30, repository.observeThresholdPercentage().first())
        }

    @Test
    fun `saveThresholdPercentage を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveThresholdPercentage(80)
            repository.saveThresholdPercentage(50)
            assertEquals(50, repository.observeThresholdPercentage().first())
        }

    @Test
    fun `文言の既定値と保存した空文字や文言を取得できる`() =
        runTest {
            assertEquals(
                LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT,
                repository.observeReadoutText().first(),
            )
            repository.saveThresholdPercentage(50)
            listOf("残量{percent}%", "", " ", "タイヤ警告").forEach { text ->
                repository.saveReadoutText(text)
                assertEquals(text, repository.observeReadoutText().first())
                assertEquals(50, repository.observeThresholdPercentage().first())
            }
            repository.saveThresholdPercentage(80)
            assertEquals("タイヤ警告", repository.observeReadoutText().first())
        }
}
