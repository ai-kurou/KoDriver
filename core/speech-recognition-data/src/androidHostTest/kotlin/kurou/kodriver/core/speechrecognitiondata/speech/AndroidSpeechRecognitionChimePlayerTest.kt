@file:Suppress("FunctionNaming")

package kurou.kodriver.core.speechrecognitiondata.speech

import android.media.AudioTrack
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
            ).play()

            assertEquals(1, received.size)
            assertEquals(wakeWordChimePcm().toList(), received.single().toList())
            assertEquals(WAKE_WORD_CHIME_DURATION_MS.toLong(), currentTime)
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
                    AndroidSpeechRecognitionChimePlayer(createTrack = { track }).play()
                }

            assertEquals("play failed", error.message)
            verify(exactly = 1) { track.play() }
            verify(exactly = 1) { track.release() }
            confirmVerified(track)
        }
}
