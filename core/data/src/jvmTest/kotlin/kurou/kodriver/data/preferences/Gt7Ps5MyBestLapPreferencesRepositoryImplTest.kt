package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5MyBestLapPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_my_best_lap_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = MyBestLapPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = Gt7Ps5MyBestLapPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `readoutText の初期値は 既定文言`() =
        runTest {
            assertEquals(GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT, repository.observeReadoutText().first())
        }

    @Test
    fun `saveReadoutText で保存した値を observeReadoutText で取得できる`() =
        runTest {
            repository.saveReadoutText("更新{laptime}")
            assertEquals("更新{laptime}", repository.observeReadoutText().first())
        }

    @Test
    fun `saveReadoutText を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveReadoutText("更新{laptime}")
            repository.saveReadoutText(GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT)
            assertEquals(GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT, repository.observeReadoutText().first())
        }

    @Test
    fun `文言の保存は既存の口調設定を保持する`() =
        runTest {
            dataStore.updateData { it.copy(voiceType = "casual") }
            repository.saveReadoutText("")
            assertEquals("", repository.observeReadoutText().first())
            assertEquals("casual", dataStore.data.first().voiceType)
        }
}
