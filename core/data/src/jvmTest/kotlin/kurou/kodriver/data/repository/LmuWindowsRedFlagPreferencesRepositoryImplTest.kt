package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.RedFlagPreferencesSerializer
import kurou.kodriver.domain.model.RedFlagVoiceType
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsRedFlagPreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_lmu_windows_red_flag_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = RedFlagPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = LmuWindowsRedFlagPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `voiceType の初期値は SESSION_STOP`() = testScope.runTest {
        assertEquals(RedFlagVoiceType.SESSION_STOP, repository.observeVoiceType().first())
    }

    @Test
    fun `saveVoiceType で保存した値を observeVoiceType で取得できる`() = testScope.runTest {
        repository.saveVoiceType(RedFlagVoiceType.RED_FLAG)
        assertEquals(RedFlagVoiceType.RED_FLAG, repository.observeVoiceType().first())
    }

    @Test
    fun `saveVoiceType を複数回呼ぶと最後の値で上書きされる`() = testScope.runTest {
        repository.saveVoiceType(RedFlagVoiceType.RED_FLAG)
        repository.saveVoiceType(RedFlagVoiceType.SESSION_STOP)
        assertEquals(RedFlagVoiceType.SESSION_STOP, repository.observeVoiceType().first())
    }

    @Test
    fun `voiceType が未知の ID のとき SESSION_STOP を返す`() = testScope.runTest {
        dataStore.updateData { it.copy(voiceType = "unknown") }
        assertEquals(RedFlagVoiceType.SESSION_STOP, repository.observeVoiceType().first())
    }
}
