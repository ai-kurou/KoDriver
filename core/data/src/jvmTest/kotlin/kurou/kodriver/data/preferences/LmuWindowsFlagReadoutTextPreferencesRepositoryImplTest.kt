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
class LmuWindowsFlagReadoutTextPreferencesRepositoryImplTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_flag_readout_text_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = FlagReadoutTextPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsFlagReadoutTextPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `sectorYellowFlagText の初期値は空文字`() =
        runTest {
            assertEquals("", repository.observeSectorYellowFlagText().first())
        }

    @Test
    fun `saveSectorYellowFlagText で保存した値を observeSectorYellowFlagText で取得できる`() =
        runTest {
            repository.saveSectorYellowFlagText("イエロー、注意")

            assertEquals("イエロー、注意", repository.observeSectorYellowFlagText().first())
        }

    @Test
    fun `空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveSectorYellowFlagText("イエロー、注意")
            repository.saveSectorYellowFlagText("")

            assertEquals("", repository.observeSectorYellowFlagText().first())
        }
}
