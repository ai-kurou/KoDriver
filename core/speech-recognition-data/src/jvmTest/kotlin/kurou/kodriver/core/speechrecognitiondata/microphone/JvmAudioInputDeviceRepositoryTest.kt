package kurou.kodriver.core.speechrecognitiondata.microphone

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AudioInputDevice
import kotlin.test.Test
import kotlin.test.assertEquals

class JvmAudioInputDeviceRepositoryTest {
    @Test
    fun `入力デバイスの名前をデバイスに変換して返す`() =
        runTest {
            val repository =
                JvmAudioInputDeviceRepository(
                    FakeAudioInputSource(names = listOf("マイク A", "マイク B")),
                    StandardTestDispatcher(testScheduler),
                )

            assertEquals(listOf(AudioInputDevice("マイク A"), AudioInputDevice("マイク B")), repository.getDevices())
        }

    @Test
    fun `デバイスがなければ空を返す`() =
        runTest {
            val source = FakeAudioInputSource()
            val repository = JvmAudioInputDeviceRepository(source, StandardTestDispatcher(testScheduler))

            assertEquals(emptyList(), repository.getDevices())
        }
}
