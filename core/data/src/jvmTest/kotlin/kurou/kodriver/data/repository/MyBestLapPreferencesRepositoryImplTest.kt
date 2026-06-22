package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.MyBestLapPreferencesSerializer
import kurou.kodriver.domain.model.MyBestLapVoiceType
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MyBestLapPreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_my_best_lap_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = MyBestLapPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = MyBestLapPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `voiceType の初期値は FORMAL`() = testScope.runTest {
        assertEquals(MyBestLapVoiceType.FORMAL, repository.observeVoiceType().first())
    }

    @Test
    fun `saveVoiceType で保存した値を observeVoiceType で取得できる`() = testScope.runTest {
        repository.saveVoiceType(MyBestLapVoiceType.CASUAL)
        assertEquals(MyBestLapVoiceType.CASUAL, repository.observeVoiceType().first())
    }

    @Test
    fun `saveVoiceType を複数回呼ぶと最後の値で上書きされる`() = testScope.runTest {
        repository.saveVoiceType(MyBestLapVoiceType.CASUAL)
        repository.saveVoiceType(MyBestLapVoiceType.FORMAL)
        assertEquals(MyBestLapVoiceType.FORMAL, repository.observeVoiceType().first())
    }

    @Test
    fun `voiceType が未知の ID のとき FORMAL を返す`() = testScope.runTest {
        dataStore.updateData { it.copy(voiceType = "unknown") }
        assertEquals(MyBestLapVoiceType.FORMAL, repository.observeVoiceType().first())
    }
}
