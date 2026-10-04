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
class AceWindowsFlagReadoutTextPreferencesRepositoryImplTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_ace_windows_flag_readout_text_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = AceWindowsFlagReadoutTextPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = AceWindowsFlagReadoutTextPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `checkeredFlagText の初期値はチェッカーフラッグ`() =
        runTest {
            assertEquals("チェッカーフラッグ", repository.observeCheckeredFlagText().first())
        }

    @Test
    fun `saveCheckeredFlagText で保存した値を observeCheckeredFlagText で取得できる`() =
        runTest {
            repository.saveCheckeredFlagText("チェッカー、完走")

            assertEquals("チェッカー、完走", repository.observeCheckeredFlagText().first())
        }

    @Test
    fun `空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveCheckeredFlagText("チェッカー、完走")
            repository.saveCheckeredFlagText("")

            assertEquals("", repository.observeCheckeredFlagText().first())
        }
}
