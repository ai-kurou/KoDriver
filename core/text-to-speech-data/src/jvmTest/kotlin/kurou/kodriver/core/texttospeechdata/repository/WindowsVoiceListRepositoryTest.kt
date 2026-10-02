package kurou.kodriver.core.texttospeechdata.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.TextToSpeechVoice
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WindowsVoiceListRepositoryTest {
    private val voices = listOf(TextToSpeechVoice("SAPI Name", "日本語音声", "ja-JP"))

    @Test
    fun `非空の一覧を保持しIOスレッドで取得する`() =
        runTest {
            val callerThread = Thread.currentThread()
            val synthesizer = FakeWindowsSpeechSynthesizer(voices = voices)
            val repository = WindowsVoiceListRepository(synthesizer)

            assertEquals(voices, repository.availableVoices())
            synthesizer.voices = emptyList()
            assertEquals(voices, repository.availableVoices())
            assertEquals(1, synthesizer.listVoicesCallCount)
            assertNotEquals(callerThread, synthesizer.listVoicesThread)
        }

    @Test
    fun `空の一覧は保持せず次回取得に成功したら保持する`() =
        runTest {
            val synthesizer = FakeWindowsSpeechSynthesizer()
            val repository = WindowsVoiceListRepository(synthesizer)

            assertEquals(emptyList(), repository.availableVoices())
            assertEquals(emptyList(), repository.availableVoices())
            synthesizer.voices = voices
            assertEquals(voices, repository.availableVoices())
            assertEquals(voices, repository.availableVoices())
            assertEquals(3, synthesizer.listVoicesCallCount)
        }

    @Test
    fun `同時の呼び出しは一覧取得を一度だけ行う`() =
        runTest {
            val release = CountDownLatch(1)
            val synthesizer = FakeWindowsSpeechSynthesizer(voices = voices, listVoicesRelease = release)
            val repository = WindowsVoiceListRepository(synthesizer)
            val first = async { repository.availableVoices() }
            runCurrent()
            try {
                assertTrue(synthesizer.listVoicesStarted.await(5, TimeUnit.SECONDS))
                val second = async { repository.availableVoices() }
                runCurrent()
                release.countDown()
                assertEquals(voices, first.await())
                assertEquals(voices, second.await())
                assertEquals(1, synthesizer.listVoicesCallCount)
            } finally {
                release.countDown()
            }
        }
}
