package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AndroidAudioUsage
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidAudioUsagePreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_android_audio_usage_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト用途を取得できる`() =
        runTest {
            val repository = createAndroidAudioUsagePreferencesRepository(tempDir.absolutePath)

            assertEquals(AndroidAudioUsage.MEDIA, repository.observeAndroidAudioUsage().first())
        }

    @Test
    fun `保存した用途を取得できる`() =
        runTest {
            val repository = createAndroidAudioUsagePreferencesRepository(tempDir.absolutePath)
            repository.saveAndroidAudioUsage(AndroidAudioUsage.RINGTONE)

            assertEquals(AndroidAudioUsage.RINGTONE, repository.observeAndroidAudioUsage().first())
        }
}
