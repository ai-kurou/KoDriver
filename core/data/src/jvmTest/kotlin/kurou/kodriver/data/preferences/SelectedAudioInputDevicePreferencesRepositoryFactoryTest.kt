package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SelectedAudioInputDevicePreferencesRepositoryFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_selected_audio_input_device_preferences_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は未指定`() =
        runTest {
            val repository = createSelectedAudioInputDevicePreferencesRepository(tempDir.absolutePath)

            assertEquals(SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED, repository.selectedDeviceName().first())
        }

    @Test
    fun `保存したデバイス名を読み出せる`() =
        runTest {
            val repository = createSelectedAudioInputDevicePreferencesRepository(tempDir.absolutePath)
            repository.saveSelectedDeviceName("マイク (USB Audio)")

            assertEquals("マイク (USB Audio)", repository.selectedDeviceName().first())
        }
}
