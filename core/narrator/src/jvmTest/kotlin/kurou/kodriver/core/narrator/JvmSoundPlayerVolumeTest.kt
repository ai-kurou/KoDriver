package kurou.kodriver.core.narrator

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JvmSoundPlayerVolumeTest {
    private val audio = FakeJvmAudioBackend()
    private val errors = mutableListOf<Throwable>()

    @Test
    fun `音量0以下は最小ゲインに設定する`() =
        runTest {
            for (volume in listOf(0, -1)) {
                JvmSoundPlayer(audio, StandardTestDispatcher(testScheduler), errors::add).play(byteArrayOf(1), volume)
                assertEquals(-80f, audio.line.gain.value)
            }
        }

    @Test
    fun `音量100は0dBで半分は対数変換したゲインになる`() =
        runTest {
            val player = JvmSoundPlayer(audio, StandardTestDispatcher(testScheduler), errors::add)

            player.play(byteArrayOf(1), 100)
            assertEquals(0f, audio.line.gain.value)
            player.play(byteArrayOf(1), 50)
            assertEquals(-6.0206f, audio.line.gain.value, 0.0001f)
        }

    @Test
    fun `正の音量でもゲイン下限を下回る場合は下限へ制限する`() =
        runTest {
            audio.line = FakeSourceDataLine(audio.format, minimumGain = -10f)

            JvmSoundPlayer(audio, StandardTestDispatcher(testScheduler), errors::add).play(byteArrayOf(1), 1)

            assertEquals(-10f, audio.line.gain.value)
        }

    @Test
    fun `過大な音量はゲインの上限に制限する`() =
        runTest {
            JvmSoundPlayer(audio, StandardTestDispatcher(testScheduler), errors::add).play(byteArrayOf(1), 1000)

            assertEquals(6f, audio.line.gain.value)
        }

    @Test
    fun `ゲイン制御非対応でも音声を再生する`() =
        runTest {
            audio.line.supportsGain = false

            JvmSoundPlayer(audio, StandardTestDispatcher(testScheduler), errors::add).play(byteArrayOf(1), 50)

            assertFalse("getGain" in audio.line.calls)
            assertTrue("drain" in audio.line.calls)
            assertTrue(errors.isEmpty())
        }
}
