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
    fun `音声の追加と削除と既定の変更を毎回IOスレッドで取得する`() =
        runTest {
            val callerThread = Thread.currentThread()
            val synthesizer = FakeWindowsSpeechSynthesizer(voices = voices)
            val repository = WindowsVoiceListRepository(synthesizer)

            assertEquals(voices, repository.availableVoices())
            val added = TextToSpeechVoice("added", "追加音声", "ja-JP", isDefault = true)
            synthesizer.voices = voices + added
            assertEquals(voices + added, repository.availableVoices())
            synthesizer.voices = listOf(added.copy(isDefault = false))
            assertEquals(synthesizer.voices, repository.availableVoices())
            synthesizer.voices = emptyList()
            assertEquals(emptyList(), repository.availableVoices())
            assertEquals(4, synthesizer.listVoicesCallCount)
            assertNotEquals(callerThread, synthesizer.listVoicesThread)
        }

    @Test
    fun `空の一覧でも次回に再取得する`() =
        runTest {
            val synthesizer = FakeWindowsSpeechSynthesizer()
            val repository = WindowsVoiceListRepository(synthesizer)

            assertEquals(emptyList(), repository.availableVoices())
            assertEquals(emptyList(), repository.availableVoices())
            synthesizer.voices = voices
            assertEquals(voices, repository.availableVoices())
            assertEquals(voices, repository.availableVoices())
            assertEquals(4, synthesizer.listVoicesCallCount)
        }

    @Test
    fun `対象言語の音声がない場合も一覧をそのまま返し毎回再取得する`() =
        runTest {
            val english = listOf(TextToSpeechVoice("Zira", "English voice", "en-US"))
            val synthesizer = FakeWindowsSpeechSynthesizer(voices = english)
            val repository = WindowsVoiceListRepository(synthesizer)

            assertEquals(english, repository.availableVoices())
            synthesizer.voices = english + voices
            assertEquals(english + voices, repository.availableVoices())
            synthesizer.voices = emptyList()
            assertEquals(emptyList(), repository.availableVoices())
            assertEquals(3, synthesizer.listVoicesCallCount)
        }

    @Test
    fun `同時の呼び出しは一覧取得を排他的に行う`() =
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
                assertEquals(1, synthesizer.listVoicesCallCount)
                release.countDown()
                assertEquals(voices, first.await())
                assertEquals(voices, second.await())
                assertEquals(2, synthesizer.listVoicesCallCount)
            } finally {
                release.countDown()
            }
        }
}
