package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AndroidAudioUsage
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidAudioUsagePreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_android_audio_usage_repo_test").toFile()
    private val dataStore =
        DataStoreFactory.create(
            serializer = AndroidAudioUsagePreferencesSerializer,
            scope = CoroutineScope(UnconfinedTestDispatcher()),
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = AndroidAudioUsagePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値はメディア`() =
        runTest {
            assertEquals(AndroidAudioUsage.MEDIA, repository.observeAndroidAudioUsage().first())
        }

    @Test
    fun `保存した用途を監視できる`() =
        runTest {
            repository.saveAndroidAudioUsage(AndroidAudioUsage.COMMUNICATION)

            assertEquals(AndroidAudioUsage.COMMUNICATION, repository.observeAndroidAudioUsage().first())
        }

    @Test
    fun `複数回保存すると最後の用途になる`() =
        runTest {
            repository.saveAndroidAudioUsage(AndroidAudioUsage.ALARM)
            repository.saveAndroidAudioUsage(AndroidAudioUsage.ASSISTANT)

            assertEquals(AndroidAudioUsage.ASSISTANT, repository.observeAndroidAudioUsage().first())
        }

    @Test
    fun `未知の保存値はメディアになる`() =
        runTest {
            dataStore.updateData { it.copy(usage = "unknown") }

            assertEquals(AndroidAudioUsage.MEDIA, repository.observeAndroidAudioUsage().first())
        }
}
