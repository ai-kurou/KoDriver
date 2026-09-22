package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.MyBestLapVoiceType
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsMyBestLapPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_lmu_windows_my_best_lap_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = MyBestLapPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsMyBestLapPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `voiceType の初期値は FORMAL`() =
        runTest {
            assertEquals(MyBestLapVoiceType.FORMAL, repository.observeVoiceType().first())
        }

    @Test
    fun `saveVoiceType で保存した値を observeVoiceType で取得できる`() =
        runTest {
            repository.saveVoiceType(MyBestLapVoiceType.CASUAL)
            assertEquals(MyBestLapVoiceType.CASUAL, repository.observeVoiceType().first())
        }

    @Test
    fun `saveVoiceType を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveVoiceType(MyBestLapVoiceType.CASUAL)
            repository.saveVoiceType(MyBestLapVoiceType.FORMAL)
            assertEquals(MyBestLapVoiceType.FORMAL, repository.observeVoiceType().first())
        }

    @Test
    fun `voiceType が未知の ID のとき FORMAL を返す`() =
        runTest {
            dataStore.updateData { it.copy(voiceType = "unknown") }
            assertEquals(MyBestLapVoiceType.FORMAL, repository.observeVoiceType().first())
        }
}
