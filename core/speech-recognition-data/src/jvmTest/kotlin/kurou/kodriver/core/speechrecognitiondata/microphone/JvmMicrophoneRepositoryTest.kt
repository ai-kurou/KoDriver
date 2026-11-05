package kurou.kodriver.core.speechrecognitiondata.microphone

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.model.MicrophoneFailureReason
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JvmMicrophoneRepositoryTest {
    private fun TestScope.repository(source: AudioInputSource) =
        JvmMicrophoneRepository(source, StandardTestDispatcher(testScheduler))

    private fun pcm(
        value: Byte,
        size: Int = 3_200,
    ) = ByteArray(size) { value }

    @Test
    fun `デバイスを開けなければUnavailableを流して完了する`() =
        runTest {
            val source = FakeAudioInputSource()

            val events = repository(source).observeAudio(AudioInputDevice("存在しない")).toList()

            assertEquals(listOf(MicrophoneAudioEvent.Failed(MicrophoneFailureReason.Unavailable)), events)
            assertEquals(listOf<String?>("存在しない"), source.openedNames)
        }

    @Test
    fun `デバイス未指定なら既定のデバイスを開く`() =
        runTest {
            val source = FakeAudioInputSource()

            repository(source).observeAudio(null).toList()

            assertEquals(listOf<String?>(null), source.openedNames)
        }

    @Test
    fun `読み込んだ音声を断片として流し、読めなくなったらUnavailableで完了して閉じる`() =
        runTest {
            val first = pcm(1)
            val second = pcm(2, size = 100)
            val capture = FakeAudioCapture(listOf(first, second))

            val source = FakeAudioInputSource(capture = capture)
            val events = repository(source).observeAudio(AudioInputDevice("マイク")).toList()

            assertEquals(3, events.size)
            assertContentEquals(first, (events[0] as MicrophoneAudioEvent.Chunk).pcm)
            assertContentEquals(second, (events[1] as MicrophoneAudioEvent.Chunk).pcm)
            assertEquals(MicrophoneAudioEvent.Failed(MicrophoneFailureReason.Unavailable), events[2])
            assertTrue(capture.closed)
        }

    @Test
    fun `読み込みが0バイトなら断片を流さず読み直す`() =
        runTest {
            val capture = FakeAudioCapture(listOf(ByteArray(0), pcm(1, size = 10)))

            val events = repository(FakeAudioInputSource(capture = capture)).observeAudio(null).toList()

            assertEquals(2, events.size)
            assertContentEquals(pcm(1, size = 10), (events[0] as MicrophoneAudioEvent.Chunk).pcm)
        }

    @Test
    fun `完全な無音が5秒続いたらNoSignalで完了して閉じる`() =
        runTest {
            val capture = FakeAudioCapture(List(100) { pcm(0) })

            val events = repository(FakeAudioInputSource(capture = capture)).observeAudio(null).toList()

            assertEquals(50, events.size)
            assertEquals(MicrophoneAudioEvent.Failed(MicrophoneFailureReason.NoSignal), events.last())
            assertTrue(events.dropLast(1).all { it is MicrophoneAudioEvent.Chunk })
            assertTrue(capture.closed)
        }

    @Test
    fun `無音の途中で音があれば無音の計測をやり直す`() =
        runTest {
            val reads = List(40) { pcm(0) } + pcm(1) + List(40) { pcm(0) }
            val capture = FakeAudioCapture(reads)

            val events = repository(FakeAudioInputSource(capture = capture)).observeAudio(null).toList()

            assertEquals(reads.size + 1, events.size)
            assertEquals(MicrophoneAudioEvent.Failed(MicrophoneFailureReason.Unavailable), events.last())
        }

    @Test
    fun `収集をやめるとデバイスを閉じる`() =
        runTest {
            val capture = FakeAudioCapture(List(10) { pcm(1) })

            repository(FakeAudioInputSource(capture = capture)).observeAudio(null).first()
            testScheduler.advanceUntilIdle()

            assertTrue(capture.closed)
        }

    @Test
    fun `0バイトの読み込みが続いたらUnavailableで完了して閉じる`() =
        runTest {
            val capture = FakeAudioCapture(List(200) { ByteArray(0) })

            val events = repository(FakeAudioInputSource(capture = capture)).observeAudio(null).toList()

            assertEquals(listOf(MicrophoneAudioEvent.Failed(MicrophoneFailureReason.Unavailable)), events)
            assertTrue(capture.closed)
        }

    @Test
    fun `読み込みがブロックしていてもキャンセルでデバイスを閉じる`() =
        runBlocking {
            val capture = BlockingAudioCapture()
            val repository = JvmMicrophoneRepository(FakeAudioInputSource(capture = capture), Dispatchers.IO)

            val job = launch(Dispatchers.Default) { repository.observeAudio(null).collect {} }
            assertTrue(capture.readStarted.await(5, TimeUnit.SECONDS))
            withTimeout(5_000) { job.cancelAndJoin() }

            assertTrue(capture.closed)
        }
}
