package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AndroidAudioUsage
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidAudioUsagePreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_android_audio_usage_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `DataStoreを作成してデフォルト値を読み込める`() =
        runTest {
            val dataStore = createAndroidAudioUsagePreferencesDataStore(tempDir.absolutePath)

            assertEquals(AndroidAudioUsage.MEDIA.id, dataStore.data.first().usage)
        }
}
