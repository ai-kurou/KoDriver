@file:Suppress("FunctionNaming")

package kurou.kodriver.core.speechrecognitiondata.speech

import android.media.AudioTrack
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.SpeechRecognitionChime
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AndroidSpeechRecognitionChimePlayerTest {
    private val track: AudioTrack = mockk()

    @Test
    fun `確認音を載せたトラックを再生し、再生時間だけ待って解放する`() =
        runTest {
            val received = mutableListOf<ShortArray>()
            every { track.play() } returns Unit
            every { track.release() } returns Unit

            AndroidSpeechRecognitionChimePlayer(
                createTrack = {
                    received += it
                    track
                },
            ).play(SpeechRecognitionChime.WakeWordDetected)

            assertEquals(1, received.size)
            assertEquals(wakeWordChimePcm().toList(), received.single().toList())
            assertEquals(WAKE_WORD_CHIME_DURATION_MS.toLong(), currentTime)
            verify(exactly = 1) { track.play() }
            verify(exactly = 1) { track.release() }
            confirmVerified(track)
        }

    @Test
    fun `時間切れの確認音は下降音の波形を載せ、その再生時間だけ待つ`() =
        runTest {
            val received = mutableListOf<ShortArray>()
            every { track.play() } returns Unit
            every { track.release() } returns Unit

            AndroidSpeechRecognitionChimePlayer(
                createTrack = {
                    received += it
                    track
                },
            ).play(SpeechRecognitionChime.CommandTimedOut)

            assertEquals(commandTimeoutChimePcm().toList(), received.single().toList())
            assertEquals(COMMAND_TIMEOUT_CHIME_DURATION_MS.toLong(), currentTime)
            verify(exactly = 1) { track.play() }
            verify(exactly = 1) { track.release() }
            confirmVerified(track)
        }

    @Test
    fun `再生の開始に失敗してもトラックを解放して例外を伝える`() =
        runTest {
            every { track.play() } throws IllegalStateException("play failed")
            every { track.release() } returns Unit

            val error =
                assertFailsWith<IllegalStateException> {
                    AndroidSpeechRecognitionChimePlayer(createTrack = { track })
                        .play(SpeechRecognitionChime.WakeWordDetected)
                }

            assertEquals("play failed", error.message)
            verify(exactly = 1) { track.play() }
            verify(exactly = 1) { track.release() }
            confirmVerified(track)
        }

    @Test
    fun `トラックの生成中に取り消されても再生せずにトラックを解放する`() =
        runTest {
            every { track.release() } returns Unit
            var job: Job? = null
            job =
                launch {
                    AndroidSpeechRecognitionChimePlayer(
                        createTrack = {
                            job?.cancel()
                            track
                        },
                    ).play(SpeechRecognitionChime.WakeWordDetected)
                }

            job.join()

            assertEquals(true, job.isCancelled)
            verify(exactly = 1) { track.release() }
            confirmVerified(track)
        }

    @Test
    fun `既定のトラック生成は静的トラックを作り、出力機器の無いホスト環境では再生開始で失敗する`() =
        runTest {
            val error =
                assertFailsWith<IllegalStateException> {
                    AndroidSpeechRecognitionChimePlayer().play(SpeechRecognitionChime.WakeWordDetected)
                }

            assertEquals("play() called on uninitialized AudioTrack.", error.message)
        }
}
