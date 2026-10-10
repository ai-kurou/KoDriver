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
class VoicePitchPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_voice_pitch_repo_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = VoicePitchPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = VoicePitchPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は1_0・保存した値を返す・上書きで更新される`() =
        runTest {
            assertEquals(1.0f, repository.voicePitch().first())

            repository.saveVoicePitch(1.5f)
            assertEquals(1.5f, repository.voicePitch().first())

            repository.saveVoicePitch(0.5f)
            assertEquals(0.5f, repository.voicePitch().first())
        }
}
