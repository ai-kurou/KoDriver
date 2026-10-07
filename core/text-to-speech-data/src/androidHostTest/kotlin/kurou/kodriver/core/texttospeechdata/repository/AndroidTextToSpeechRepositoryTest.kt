@file:Suppress("FunctionNaming", "TooManyFunctions")

package kurou.kodriver.core.texttospeechdata.repository

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidTextToSpeechRepositoryTest {
    private val voice: Voice = mockk()
    private val customVoice: Voice = mockk()
    private val englishVoice: Voice = mockk()
    private val textToSpeech: TextToSpeech = mockk(relaxUnitFun = true)
    private var factoryCallCount = 0
    private val listenerSlot = slot<UtteranceProgressListener>()
    private val params: Bundle = mockk()
    private val requestedVolumes = mutableListOf<Float>()
    private val volumeParamsFactory: (Float) -> Bundle = { volume ->
        requestedVolumes += volume
        params
    }

    private val createRepository: (Int) -> AndroidTextToSpeechRepository = { initStatus ->
        AndroidTextToSpeechRepository(factory(initStatus), volumeParamsFactory = volumeParamsFactory)
    }

    /** `OnInitListener` を同期的に [status] で呼び出してから [textToSpeech] を返すFakeのファクトリ。 */
    private fun factory(status: Int): (TextToSpeech.OnInitListener) -> TextToSpeech =
        { listener ->
            factoryCallCount++
            listener.onInit(status)
            textToSpeech
        }

    @Test
    fun `端末の既定が英語でも日本語の初期音声を保持し個別音声の指定で変えない`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns voice
            every { textToSpeech.defaultVoice } returns englishVoice
            every { voice.name } returns "japanese-default"
            every { customVoice.name } returns "custom"
            every { textToSpeech.voices } returns setOf(customVoice)
            every { textToSpeech.setVoice(customVoice) } answers {
                every { textToSpeech.voice } returns customVoice
                TextToSpeech.SUCCESS
            }
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            every {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            } returns TextToSpeech.ERROR
            val repository = createRepository(TextToSpeech.SUCCESS)
            val voiceListRepository =
                AndroidVoiceListRepository(
                    engineProvider = { repository.engineOrNull() },
                    defaultVoiceIdProvider = { repository.defaultVoiceId },
                )

            assertNull(repository.defaultVoiceId)
            repository.engineOrNull()
            assertEquals("japanese-default", repository.defaultVoiceId)
            repository.speak("読み上げ", queue = true, voiceId = "custom")
            assertEquals("japanese-default", repository.defaultVoiceId)
            // 再取得でも個別指定の音声を既定と誤認しない。
            every { customVoice.locale } returns Locale.JAPANESE
            every { customVoice.isNetworkConnectionRequired } returns false
            every { customVoice.features } returns emptySet()
            assertFalse(voiceListRepository.availableVoices().single().isDefault)

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 0) { textToSpeech.defaultVoice }
            verify(exactly = 1) { voice.name }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 2) { textToSpeech.voices }
            verify(exactly = 1) { textToSpeech.setVoice(customVoice) }
            verify(exactly = 4) { customVoice.name }
            verify(exactly = 1) { customVoice.locale }
            verify(exactly = 1) { customVoice.isNetworkConnectionRequired }
            verify(exactly = 1) { customVoice.features }
            verify(exactly = 1) {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            }
            confirmVerified(textToSpeech, voice, customVoice, englishVoice)
        }

    @Test
    fun `初期音声の取得に失敗しても利用可能でIDは不明とする`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } throws IllegalStateException("voice")
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)

            assertTrue(repository.isAvailable())
            assertNull(repository.defaultVoiceId)

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `初期音声の取得中のキャンセルは再スローする`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } throws CancellationException("voice")
            val repository = createRepository(TextToSpeech.SUCCESS)

            assertFailsWith<CancellationException> { repository.isAvailable() }
            assertNull(repository.defaultVoiceId)

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.voice }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `初期化に成功した場合はspeakでテキストを読み上げる`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_ADD, params, capture(utteranceIdSlot))
            } answers {
                listenerSlot.captured.onDone(utteranceIdSlot.captured)
                TextToSpeech.SUCCESS
            }
            val repository = createRepository(TextToSpeech.SUCCESS)

            repository.speak("ベストラップ", queue = true, volume = 40)

            assertEquals(listOf(0.4f), requestedVolumes)
            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_ADD, params, utteranceIdSlot.captured)
            }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `読み上げが完了するまでspeakはsuspendする`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_FLUSH, params, capture(utteranceIdSlot))
            } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)
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
            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_FLUSH, params, utteranceIdSlot.captured)
            }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `読み上げエラーでもspeakは完了する`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_FLUSH, params, capture(utteranceIdSlot))
            } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)
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
            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_FLUSH, params, utteranceIdSlot.captured)
            }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `再生中にキャンセルされた場合は読み上げを停止する`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_FLUSH, params, capture(utteranceIdSlot))
            } returns TextToSpeech.SUCCESS
            every { textToSpeech.stop() } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)

            val job = launch { repository.speak("ベストラップ", queue = false) }
            runCurrent()
            listenerSlot.captured.onStart(utteranceIdSlot.captured)

            job.cancel()
            job.join()

            assertTrue(job.isCancelled)
            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_FLUSH, params, utteranceIdSlot.captured)
            }
            verify(exactly = 1) { textToSpeech.stop() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `発話要求自体が失敗した場合はハングせず即座に完了する`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_FLUSH, params, capture(utteranceIdSlot))
            } returns TextToSpeech.ERROR
            val repository = createRepository(TextToSpeech.SUCCESS)

            repository.speak("ベストラップ", queue = false)

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_FLUSH, params, utteranceIdSlot.captured)
            }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `stopによる打ち切りでonStopが呼ばれた場合もspeakは完了する`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_FLUSH, params, capture(utteranceIdSlot))
            } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)
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
            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_FLUSH, params, utteranceIdSlot.captured)
            }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `新しい発話要求は打ち切られる古い発話をonStop等を待たず即座に完了させる`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every {
                textToSpeech.speak("古い発話", TextToSpeech.QUEUE_FLUSH, params, capture(utteranceIdSlot))
            } returns TextToSpeech.SUCCESS andThen TextToSpeech.SUCCESS
            val newUtteranceIdSlot = slot<String>()
            every {
                textToSpeech.speak("新しい発話", TextToSpeech.QUEUE_FLUSH, params, capture(newUtteranceIdSlot))
            } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)
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
            val newUtteranceId = newUtteranceIdSlot.captured

            assertTrue(oldCompleted)
            oldJob.join()

            listenerSlot.captured.onDone(newUtteranceId)
            newJob.join()

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) {
                textToSpeech.speak("古い発話", TextToSpeech.QUEUE_FLUSH, params, utteranceIdSlot.captured)
            }
            verify(exactly = 1) {
                textToSpeech.speak("新しい発話", TextToSpeech.QUEUE_FLUSH, params, newUtteranceId)
            }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `再生開始前にキャンセルされた場合は再生中の別の発話を止めない`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every {
                textToSpeech.speak("A", TextToSpeech.QUEUE_FLUSH, params, capture(utteranceIdSlot))
            } returns TextToSpeech.SUCCESS andThen TextToSpeech.SUCCESS
            val utteranceIdBSlot = slot<String>()
            every {
                textToSpeech.speak("B", TextToSpeech.QUEUE_ADD, params, capture(utteranceIdBSlot))
            } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)

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

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) {
                textToSpeech.speak("A", TextToSpeech.QUEUE_FLUSH, params, utteranceIdA)
            }
            verify(exactly = 1) {
                textToSpeech.speak("B", TextToSpeech.QUEUE_ADD, params, utteranceIdBSlot.captured)
            }
            verify(exactly = 0) { textToSpeech.stop() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `明示的なstopはonStop等のコールバックが来なくてもpending中のspeakを完了させる`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val utteranceIdSlot = slot<String>()
            every {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_FLUSH, params, capture(utteranceIdSlot))
            } returns TextToSpeech.SUCCESS
            every { textToSpeech.stop() } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)
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

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_FLUSH, params, utteranceIdSlot.captured)
            }
            verify(exactly = 1) { textToSpeech.stop() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `初期化は最初の1回のみ行う`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)

            assertTrue(repository.isAvailable())
            assertTrue(repository.isAvailable())

            assertEquals(1, factoryCallCount)

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `利用可能な場合はunavailableReasonがnullを返す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)

            assertNull(repository.unavailableReason())

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `読み上げ言語が利用できない場合は利用不可としてエンジンを破棄しLanguageDataMissingを返す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_NOT_SUPPORTED
            val repository = createRepository(TextToSpeech.SUCCESS)

            assertEquals(TextToSpeechUnavailableReason.LanguageDataMissing, repository.unavailableReason())
            assertFalse(repository.isAvailable())
            repository.speak("ベストラップ", queue = false)

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.shutdown() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `初期化に失敗した場合は利用不可としてエンジンを破棄しEngineMissingを返す`() =
        runTest {
            val repository = createRepository(TextToSpeech.ERROR)

            assertEquals(TextToSpeechUnavailableReason.EngineMissing, repository.unavailableReason())
            assertFalse(repository.isAvailable())

            verify(exactly = 1) { textToSpeech.shutdown() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `発話完了通知リスナーの登録に失敗した場合は利用不可としてエンジンを破棄しEngineMissingを返す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.ERROR
            val repository = createRepository(TextToSpeech.SUCCESS)

            assertEquals(TextToSpeechUnavailableReason.EngineMissing, repository.unavailableReason())
            assertFalse(repository.isAvailable())

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) { textToSpeech.shutdown() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `空白のみのテキストは読み上げず初期化も行わない`() =
        runTest {
            val repository = createRepository(TextToSpeech.SUCCESS)

            repository.speak("  ", queue = false)

            assertEquals(0, factoryCallCount)
            confirmVerified(textToSpeech)
        }

    @Test
    fun `stopは初期化済みのエンジンに対してのみ停止を呼び出す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            every { textToSpeech.stop() } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)

            repository.stop()
            assertEquals(0, factoryCallCount)

            assertTrue(repository.isAvailable())
            repository.stop()

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) { textToSpeech.stop() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `利用不可だった後にunavailableReasonを呼ぶと再初期化して利用可能になればnullを返す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returnsMany
                listOf(TextToSpeech.LANG_NOT_SUPPORTED, TextToSpeech.LANG_AVAILABLE)
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)

            assertEquals(TextToSpeechUnavailableReason.LanguageDataMissing, repository.unavailableReason())
            assertFalse(repository.isAvailable())
            assertNull(repository.unavailableReason())
            assertTrue(repository.isAvailable())

            assertEquals(2, factoryCallCount)
            verify(exactly = 2) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.shutdown() }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `利用不可の間はisAvailableとspeakでは再初期化しない`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_NOT_SUPPORTED
            val repository = createRepository(TextToSpeech.SUCCESS)

            assertFalse(repository.isAvailable())
            assertFalse(repository.isAvailable())
            repository.speak("ベストラップ", queue = false)

            assertEquals(1, factoryCallCount)
            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.shutdown() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `音声IDが一致すると指定音声を適用する`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            every { textToSpeech.voices } returns setOf(voice)
            every { voice.name } returns "ja-jp-x-jab-local"
            every { textToSpeech.setVoice(voice) } returns TextToSpeech.SUCCESS
            every {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            } returns TextToSpeech.ERROR
            val repository = createRepository(TextToSpeech.SUCCESS)

            repository.speak("読み上げ", queue = true, voiceId = "ja-jp-x-jab-local")

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) { textToSpeech.voices }
            verify(exactly = 1) { voice.name }
            verify(exactly = 1) { textToSpeech.setVoice(voice) }
            verify(exactly = 1) {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            }
            confirmVerified(textToSpeech, voice)
        }

    @Test
    fun `音声IDが見つからないと日本語へ戻す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            every { textToSpeech.voices } returns emptySet()
            every {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            } returns TextToSpeech.ERROR
            val repository = createRepository(TextToSpeech.SUCCESS)

            repository.speak("読み上げ", queue = true, voiceId = "ja-jp-x-jab-local")

            verify(exactly = 2) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) { textToSpeech.voices }
            verify(exactly = 1) {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `音声の適用が失敗すると日本語へ戻す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            every { textToSpeech.voices } returns setOf(voice)
            every { voice.name } returns "ja-jp-x-jab-local"
            every { textToSpeech.setVoice(voice) } returns TextToSpeech.ERROR
            every {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            } returns TextToSpeech.ERROR
            val repository = createRepository(TextToSpeech.SUCCESS)

            repository.speak("読み上げ", queue = true, voiceId = "ja-jp-x-jab-local")

            verify(exactly = 2) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) { textToSpeech.voices }
            verify(exactly = 1) { voice.name }
            verify(exactly = 1) { textToSpeech.setVoice(voice) }
            verify(exactly = 1) {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            }
            confirmVerified(textToSpeech, voice)
        }

    @Test
    fun `同じ音声IDは重複して適用しない`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            every { textToSpeech.voices } returns setOf(voice)
            every { voice.name } returns "ja-jp-x-jab-local"
            every { textToSpeech.setVoice(voice) } returns TextToSpeech.SUCCESS
            every {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            } returns TextToSpeech.ERROR
            every {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_2")
            } returns TextToSpeech.ERROR
            val repository = createRepository(TextToSpeech.SUCCESS)

            repository.speak("読み上げ", queue = true, voiceId = "ja-jp-x-jab-local")
            repository.speak("読み上げ", queue = true, voiceId = "ja-jp-x-jab-local")

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) { textToSpeech.voices }
            verify(exactly = 1) { voice.name }
            verify(exactly = 1) { textToSpeech.setVoice(voice) }
            verify(exactly = 1) {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            }
            verify(exactly = 1) {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_2")
            }
            confirmVerified(textToSpeech, voice)
        }

    @Test
    fun `指定音声から空に戻すと日本語を再適用する`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            every { textToSpeech.voices } returns setOf(voice)
            every { voice.name } returns "ja-jp-x-jab-local"
            every { textToSpeech.setVoice(voice) } returns TextToSpeech.SUCCESS
            every {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            } returns TextToSpeech.ERROR
            every {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_2")
            } returns TextToSpeech.ERROR
            val repository = createRepository(TextToSpeech.SUCCESS)

            repository.speak("読み上げ", queue = true, voiceId = "ja-jp-x-jab-local")
            repository.speak("読み上げ", queue = true, voiceId = "")

            verify(exactly = 2) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) { textToSpeech.voices }
            verify(exactly = 1) { voice.name }
            verify(exactly = 1) { textToSpeech.setVoice(voice) }
            verify(exactly = 1) {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            }
            verify(exactly = 1) {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_2")
            }
            confirmVerified(textToSpeech, voice)
        }

    @Test
    fun `音声一覧がnullなら日本語へ戻す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            every { textToSpeech.voices } returns null
            every {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            } returns TextToSpeech.ERROR
            val repository = createRepository(TextToSpeech.SUCCESS)

            repository.speak("読み上げ", queue = true, voiceId = "ja-jp-x-jab-local")

            verify(exactly = 2) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) { textToSpeech.voices }
            verify(exactly = 1) {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `音声一覧の取得例外なら日本語へ戻す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            every { textToSpeech.voices } throws IllegalStateException("voices")
            every {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            } returns TextToSpeech.ERROR
            val repository = createRepository(TextToSpeech.SUCCESS)

            repository.speak("読み上げ", queue = true, voiceId = "ja-jp-x-jab-local")

            verify(exactly = 2) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) { textToSpeech.voices }
            verify(exactly = 1) {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `音声が見つからない場合は日本語へ戻し次の要求で再検索する`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            every { textToSpeech.voices } returns setOf(voice)
            every { voice.name } returns "別の音声"
            every {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            } returns TextToSpeech.ERROR
            every {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_2")
            } returns TextToSpeech.ERROR
            val repository = createRepository(TextToSpeech.SUCCESS)

            repository.speak("読み上げ", queue = true, voiceId = "ja-jp-x-jab-local")
            repository.speak("読み上げ", queue = true, voiceId = "ja-jp-x-jab-local")

            verify(exactly = 3) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 2) { textToSpeech.voices }
            verify(exactly = 2) { voice.name }
            verify(exactly = 0) { textToSpeech.setVoice(voice) }
            verify(exactly = 1) {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_1")
            }
            verify(exactly = 1) {
                textToSpeech.speak("読み上げ", TextToSpeech.QUEUE_ADD, params, "kodriver_tts_2")
            }
            confirmVerified(textToSpeech, voice)
        }

    @Test
    fun `engineOrNullは初期化成功時に同じエンジンを返す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            every { textToSpeech.voice } returns null
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)

            assertTrue(textToSpeech === repository.engineOrNull())
            assertTrue(textToSpeech === repository.engineOrNull())
            assertEquals(1, factoryCallCount)

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `engineOrNullは初期化失敗時にnullを返す`() =
        runTest {
            val repository = createRepository(TextToSpeech.ERROR)

            assertNull(repository.engineOrNull())

            verify(exactly = 1) { textToSpeech.shutdown() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `engineOrNullは再試行指定なしでは初期化失敗後に再初期化しない`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_NOT_SUPPORTED
            val repository = createRepository(TextToSpeech.SUCCESS)

            assertNull(repository.engineOrNull())
            assertNull(repository.engineOrNull())

            assertEquals(1, factoryCallCount)
            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.shutdown() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `engineOrNullは再試行指定ありで初期化失敗後に再初期化して成功すればエンジンを返す`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returnsMany
                listOf(TextToSpeech.LANG_NOT_SUPPORTED, TextToSpeech.LANG_AVAILABLE)
            every { textToSpeech.voice } returns voice
            every { voice.name } returns "new-default"
            every { textToSpeech.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS
            val repository = createRepository(TextToSpeech.SUCCESS)

            assertNull(repository.engineOrNull(retryIfUnavailable = true))
            assertNull(repository.defaultVoiceId)
            assertTrue(textToSpeech === repository.engineOrNull(retryIfUnavailable = true))
            assertTrue(textToSpeech === repository.engineOrNull(retryIfUnavailable = true))

            assertEquals("new-default", repository.defaultVoiceId)
            assertEquals(2, factoryCallCount)
            verify(exactly = 2) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.shutdown() }
            verify(exactly = 1) { textToSpeech.setOnUtteranceProgressListener(listenerSlot.captured) }
            verify(exactly = 1) { textToSpeech.voice }
            verify(exactly = 1) { voice.name }
            confirmVerified(textToSpeech, voice)
        }
}
