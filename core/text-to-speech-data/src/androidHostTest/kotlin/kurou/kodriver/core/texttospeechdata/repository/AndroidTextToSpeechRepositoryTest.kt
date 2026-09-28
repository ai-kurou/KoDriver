@file:Suppress("FunctionNaming")

package kurou.kodriver.core.texttospeechdata.repository

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidTextToSpeechRepositoryTest {
    private val textToSpeech: TextToSpeech = mockk(relaxUnitFun = true)
    private var factoryCallCount = 0
    private val listenerSlot = slot<UtteranceProgressListener>()

    /** `OnInitListener` を同期的に [status] で呼び出してから [textToSpeech] を返すFakeのファクトリ。 */
    private fun factory(status: Int): (TextToSpeech.OnInitListener) -> TextToSpeech =
        { listener ->
            factoryCallCount++
            listener.onInit(status)
            textToSpeech
        }

    @Test
    fun `初期化に成功した場合はspeakでテキストを読み上げる`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every {
                textToSpeech.speak(any(), any(), null, capture(utteranceIdSlot))
            } answers {
                listenerSlot.captured.onDone(utteranceIdSlot.captured)
                TextToSpeech.SUCCESS
            }
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            repository.speak("ベストラップ", queue = true)

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(any()) }
            verify(exactly = 1) { textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_ADD, null, any()) }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `読み上げが完了するまでspeakはsuspendする`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every { textToSpeech.speak(any(), any(), null, capture(utteranceIdSlot)) } returns TextToSpeech.SUCCESS
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))
            var completed = false

            val job =
                launch {
                    repository.speak("ベストラップ", queue = false)
                    completed = true
                }
            runCurrent()

            assertFalse(completed)

            listenerSlot.captured.onDone(utteranceIdSlot.captured)
            job.join()

            assertTrue(completed)
        }

    @Test
    fun `読み上げエラーでもspeakは完了する`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every { textToSpeech.speak(any(), any(), null, capture(utteranceIdSlot)) } returns TextToSpeech.SUCCESS
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))
            var completed = false

            val job =
                launch {
                    repository.speak("ベストラップ", queue = false)
                    completed = true
                }
            runCurrent()

            @Suppress("DEPRECATION")
            listenerSlot.captured.onError(utteranceIdSlot.captured)
            job.join()

            assertTrue(completed)
        }

    @Test
    fun `再生中にキャンセルされた場合は読み上げを停止する`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every { textToSpeech.speak(any(), any(), null, capture(utteranceIdSlot)) } returns TextToSpeech.SUCCESS
            every { textToSpeech.stop() } returns TextToSpeech.SUCCESS
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            val job = launch { repository.speak("ベストラップ", queue = false) }
            runCurrent()
            listenerSlot.captured.onStart(utteranceIdSlot.captured)

            job.cancel()
            job.join()

            assertTrue(job.isCancelled)
            verify(exactly = 1) { textToSpeech.stop() }
        }

    @Test
    fun `発話要求自体が失敗した場合はハングせず即座に完了する`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            every { textToSpeech.speak(any(), any(), null, any()) } returns TextToSpeech.ERROR
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            repository.speak("ベストラップ", queue = false)

            verify(exactly = 1) { textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_FLUSH, null, any()) }
        }

    @Test
    fun `stopによる打ち切りでonStopが呼ばれた場合もspeakは完了する`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every { textToSpeech.speak(any(), any(), null, capture(utteranceIdSlot)) } returns TextToSpeech.SUCCESS
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))
            var completed = false

            val job =
                launch {
                    repository.speak("ベストラップ", queue = false)
                    completed = true
                }
            runCurrent()

            listenerSlot.captured.onStop(utteranceIdSlot.captured, true)
            job.join()

            assertTrue(completed)
        }

    @Test
    fun `新しい発話要求は打ち切られる古い発話をonStop等を待たず即座に完了させる`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every { textToSpeech.speak(any(), any(), null, capture(utteranceIdSlot)) } returns TextToSpeech.SUCCESS
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))
            var oldCompleted = false

            val oldJob =
                launch {
                    repository.speak("古い発話", queue = false)
                    oldCompleted = true
                }
            runCurrent()
            assertFalse(oldCompleted)

            // 新しい発話要求（QUEUE_FLUSH）を出すと、onStop等の通知を待たず古い発話が即座に完了する。
            val newJob = launch { repository.speak("新しい発話", queue = false) }
            runCurrent()
            val newUtteranceId = utteranceIdSlot.captured

            assertTrue(oldCompleted)
            oldJob.join()

            listenerSlot.captured.onDone(newUtteranceId)
            newJob.join()
        }

    @Test
    fun `再生開始前にキャンセルされた場合は再生中の別の発話を止めない`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every { textToSpeech.speak(any(), any(), null, capture(utteranceIdSlot)) } returns TextToSpeech.SUCCESS
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            // Aが実際に再生中（onStart済み）。
            val jobA = launch { repository.speak("A", queue = false) }
            runCurrent()
            val utteranceIdA = utteranceIdSlot.captured
            listenerSlot.captured.onStart(utteranceIdA)

            // BはAの後ろにキューイングされただけで、まだ再生開始（onStart）していない。
            val jobB = launch { repository.speak("B", queue = true) }
            runCurrent()

            // Bをキャンセルしても、まだ再生中のAを巻き添えで止めない。
            jobB.cancel()
            jobB.join()
            verify(exactly = 0) { textToSpeech.stop() }

            listenerSlot.captured.onDone(utteranceIdA)
            jobA.join()
        }

    @Test
    fun `明示的なstopはonStop等のコールバックが来なくてもpending中のspeakを完了させる`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            every { textToSpeech.speak(any(), any(), null, any()) } returns TextToSpeech.SUCCESS
            every { textToSpeech.stop() } returns TextToSpeech.SUCCESS
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))
            var completed = false

            val job =
                launch {
                    repository.speak("ベストラップ", queue = false)
                    completed = true
                }
            runCurrent()
            assertFalse(completed)

            repository.stop()
            runCurrent()

            assertTrue(completed)
            job.join()
        }

    @Test
    fun `初期化は最初の1回のみ行う`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.setOnUtteranceProgressListener(any()) } returns TextToSpeech.SUCCESS
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            assertTrue(repository.isAvailable())
            assertTrue(repository.isAvailable())

            assertEquals(1, factoryCallCount)
        }

    @Test
    fun `利用可能な場合はunavailableReasonがnullを返す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.setOnUtteranceProgressListener(any()) } returns TextToSpeech.SUCCESS
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            assertNull(repository.unavailableReason())
        }

    @Test
    fun `読み上げ言語が利用できない場合は利用不可としてエンジンを破棄しLanguageDataMissingを返す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_NOT_SUPPORTED
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            assertFalse(repository.isAvailable())
            assertEquals(TextToSpeechUnavailableReason.LanguageDataMissing, repository.unavailableReason())
            repository.speak("ベストラップ", queue = false)

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.shutdown() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `初期化に失敗した場合は利用不可としてエンジンを破棄しEngineMissingを返す`() =
        runTest {
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.ERROR))

            assertFalse(repository.isAvailable())
            assertEquals(TextToSpeechUnavailableReason.EngineMissing, repository.unavailableReason())

            verify(exactly = 1) { textToSpeech.shutdown() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `発話完了通知リスナーの登録に失敗した場合は利用不可としてエンジンを破棄しEngineMissingを返す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.setOnUtteranceProgressListener(any()) } returns TextToSpeech.ERROR
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            assertFalse(repository.isAvailable())
            assertEquals(TextToSpeechUnavailableReason.EngineMissing, repository.unavailableReason())

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(any()) }
            verify(exactly = 1) { textToSpeech.shutdown() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `空白のみのテキストは読み上げず初期化も行わない`() =
        runTest {
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            repository.speak("  ", queue = false)

            assertEquals(0, factoryCallCount)
            confirmVerified(textToSpeech)
        }

    @Test
    fun `stopは初期化済みのエンジンに対してのみ停止を呼び出す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.setOnUtteranceProgressListener(any()) } returns TextToSpeech.SUCCESS
            every { textToSpeech.stop() } returns TextToSpeech.SUCCESS
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            repository.stop()
            assertEquals(0, factoryCallCount)

            assertTrue(repository.isAvailable())
            repository.stop()

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(any()) }
            verify(exactly = 1) { textToSpeech.stop() }
            confirmVerified(textToSpeech)
        }
}
