package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class VoicePreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_voice_repo_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = VoicePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = VoicePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は未指定・保存した値を返す・上書きで更新される・空文字にリセットできる`() =
        runTest {
            assertEquals(VOICE_ID_UNSPECIFIED, repository.voiceId().first())

            repository.saveVoiceId("Microsoft Haruka Desktop")
            assertEquals("Microsoft Haruka Desktop", repository.voiceId().first())

            repository.saveVoiceId("ja-jp-x-jab-local")
            assertEquals("ja-jp-x-jab-local", repository.voiceId().first())

            repository.saveVoiceId(VOICE_ID_UNSPECIFIED)
            assertEquals(VOICE_ID_UNSPECIFIED, repository.voiceId().first())
        }
}
