@file:Suppress("FunctionNaming")

package kurou.kodriver.core.texttospeechdata.repository

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.TTS_CULTURE_NAME
import kurou.kodriver.domain.model.TextToSpeechVoice
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AndroidVoiceListRepositoryTest {
    private val engine: TextToSpeech = mockk(relaxUnitFun = true)
    private val engineProvider: suspend () -> TextToSpeech? = mockk()
    private val voice: Voice = mockk()
    private val defaultVoiceIdProvider: () -> String? = mockk()
    private val repository = AndroidVoiceListRepository(engineProvider, defaultVoiceIdProvider)

    @Test
    fun `システム既定と同じIDの音声を識別し取得不能なら既定扱いにしない`() =
        runTest {
            coEvery { engineProvider() } returns engine
            every { engine.voices } returns setOf(voice)
            every { defaultVoiceIdProvider() } returns "ja-jp-x-jab-local" andThenThrows
                IllegalStateException("default")
            every { voice.locale } returns Locale.JAPANESE
            every { voice.isNetworkConnectionRequired } returns false
            every { voice.features } returns emptySet()
            every { voice.name } returns "ja-jp-x-jab-local"

            val expected = TextToSpeechVoice("ja-jp-x-jab-local", "日本語 (jab・ローカル)", TTS_CULTURE_NAME)
            assertEquals(listOf(expected.copy(isDefault = true)), repository.availableVoices())
            assertEquals(listOf(expected), repository.availableVoices())

            coVerify(exactly = 2) { engineProvider() }
            verify(exactly = 2) { engine.voices }
            verify(exactly = 2) { defaultVoiceIdProvider() }
            verify(exactly = 2) { voice.locale }
            verify(exactly = 2) { voice.isNetworkConnectionRequired }
            verify(exactly = 2) { voice.features }
            verify(exactly = 6) { voice.name }
            confirmVerified(engineProvider, engine, voice, defaultVoiceIdProvider)
        }

    @Test
    fun `既定音声取得のキャンセルは再スローする`() =
        runTest {
            coEvery { engineProvider() } returns engine
            every { engine.voices } returns emptySet()
            every { defaultVoiceIdProvider() } throws CancellationException("default")

            assertFailsWith<CancellationException> { repository.availableVoices() }

            coVerify(exactly = 1) { engineProvider() }
            verify(exactly = 1) { engine.voices }
            verify(exactly = 1) { defaultVoiceIdProvider() }
            confirmVerified(engineProvider, engine, defaultVoiceIdProvider)
        }

    @Test
    fun `日本語の音声を整形して返す`() =
        runTest {
            coEvery { engineProvider() } returns engine
            every { defaultVoiceIdProvider() } returns null
            every { engine.voices } returns setOf(voice)
            every { voice.locale } returns Locale.JAPANESE
            every { voice.isNetworkConnectionRequired } returns false
            every { voice.features } returns emptySet()
            every { voice.name } returns "ja-jp-x-jab-local"

            assertEquals(
                listOf(TextToSpeechVoice("ja-jp-x-jab-local", "日本語 (jab・ローカル)", TTS_CULTURE_NAME)),
                repository.availableVoices(),
            )

            coVerify(exactly = 1) { engineProvider() }
            verify(exactly = 1) { engine.voices }
            verify(exactly = 1) { defaultVoiceIdProvider() }
            verify(exactly = 1) { voice.locale }
            verify(exactly = 1) { voice.isNetworkConnectionRequired }
            verify(exactly = 1) { voice.features }
            verify(exactly = 3) { voice.name }
            confirmVerified(engineProvider, engine, voice, defaultVoiceIdProvider)
        }

    @Test
    fun `日本語の地域違いも一覧に含める`() =
        runTest {
            coEvery { engineProvider() } returns engine
            every { defaultVoiceIdProvider() } returns null
            every { engine.voices } returns setOf(voice)
            every { voice.locale } returns Locale.JAPAN
            every { voice.isNetworkConnectionRequired } returns false
            every { voice.features } returns null
            every { voice.name } returns "ja-jp-x-jab-local"

            assertEquals(
                listOf(TextToSpeechVoice("ja-jp-x-jab-local", "日本語 (jab・ローカル)", TTS_CULTURE_NAME)),
                repository.availableVoices(),
            )

            coVerify(exactly = 1) { engineProvider() }
            verify(exactly = 1) { engine.voices }
            verify(exactly = 1) { defaultVoiceIdProvider() }
            verify(exactly = 1) { voice.locale }
            verify(exactly = 1) { voice.isNetworkConnectionRequired }
            verify(exactly = 1) { voice.features }
            verify(exactly = 3) { voice.name }
            confirmVerified(engineProvider, engine, voice, defaultVoiceIdProvider)
        }

    @Test
    fun `日本語以外の音声は除外する`() =
        runTest {
            coEvery { engineProvider() } returns engine
            every { defaultVoiceIdProvider() } returns null
            every { engine.voices } returns setOf(voice)
            every { voice.locale } returns Locale.ENGLISH

            assertEquals(emptyList(), repository.availableVoices())

            coVerify(exactly = 1) { engineProvider() }
            verify(exactly = 1) { engine.voices }
            verify(exactly = 1) { defaultVoiceIdProvider() }
            verify(exactly = 1) { voice.locale }
            confirmVerified(engineProvider, engine, voice, defaultVoiceIdProvider)
        }

    @Test
    fun `ネットワーク必須の音声は除外する`() =
        runTest {
            coEvery { engineProvider() } returns engine
            every { defaultVoiceIdProvider() } returns null
            every { engine.voices } returns setOf(voice)
            every { voice.locale } returns Locale.JAPANESE
            every { voice.isNetworkConnectionRequired } returns true

            assertEquals(emptyList(), repository.availableVoices())

            coVerify(exactly = 1) { engineProvider() }
            verify(exactly = 1) { engine.voices }
            verify(exactly = 1) { defaultVoiceIdProvider() }
            verify(exactly = 1) { voice.locale }
            verify(exactly = 1) { voice.isNetworkConnectionRequired }
            confirmVerified(engineProvider, engine, voice, defaultVoiceIdProvider)
        }

    @Test
    fun `未インストールの音声は除外する`() =
        runTest {
            coEvery { engineProvider() } returns engine
            every { defaultVoiceIdProvider() } returns null
            every { engine.voices } returns setOf(voice)
            every { voice.locale } returns Locale.JAPANESE
            every { voice.isNetworkConnectionRequired } returns false
            every { voice.features } returns setOf(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)

            assertEquals(emptyList(), repository.availableVoices())

            coVerify(exactly = 1) { engineProvider() }
            verify(exactly = 1) { engine.voices }
            verify(exactly = 1) { defaultVoiceIdProvider() }
            verify(exactly = 1) { voice.locale }
            verify(exactly = 1) { voice.isNetworkConnectionRequired }
            verify(exactly = 1) { voice.features }
            confirmVerified(engineProvider, engine, voice, defaultVoiceIdProvider)
        }

    @Test
    fun `音声一覧がnullなら空を返す`() =
        runTest {
            coEvery { engineProvider() } returns engine
            every { defaultVoiceIdProvider() } returns null
            every { engine.voices } returns null

            assertEquals(emptyList(), repository.availableVoices())

            coVerify(exactly = 1) { engineProvider() }
            verify(exactly = 1) { engine.voices }
            verify(exactly = 1) { defaultVoiceIdProvider() }
            confirmVerified(engineProvider, engine, defaultVoiceIdProvider)
        }

    @Test
    fun `音声一覧が空なら空を返す`() =
        runTest {
            coEvery { engineProvider() } returns engine
            every { defaultVoiceIdProvider() } returns null
            every { engine.voices } returns emptySet()

            assertEquals(emptyList(), repository.availableVoices())

            coVerify(exactly = 1) { engineProvider() }
            verify(exactly = 1) { engine.voices }
            verify(exactly = 1) { defaultVoiceIdProvider() }
            confirmVerified(engineProvider, engine, defaultVoiceIdProvider)
        }

    @Test
    fun `音声一覧取得の例外なら空を返す`() =
        runTest {
            coEvery { engineProvider() } returns engine
            every { defaultVoiceIdProvider() } returns null
            every { engine.voices } throws IllegalStateException("voices")

            assertEquals(emptyList(), repository.availableVoices())

            coVerify(exactly = 1) { engineProvider() }
            verify(exactly = 1) { engine.voices }
            verify(exactly = 1) { defaultVoiceIdProvider() }
            confirmVerified(engineProvider, engine, defaultVoiceIdProvider)
        }

    @Test
    fun `エンジンがnullなら空を返す`() =
        runTest {
            coEvery { engineProvider() } returns null

            assertEquals(emptyList(), repository.availableVoices())

            coVerify(exactly = 1) { engineProvider() }
            confirmVerified(engineProvider, engine, defaultVoiceIdProvider)
        }

    @Test
    fun `音声一覧取得のキャンセルは再スローする`() =
        runTest {
            coEvery { engineProvider() } returns engine
            every { defaultVoiceIdProvider() } returns null
            every { engine.voices } throws CancellationException("voices")

            assertFailsWith<CancellationException> { repository.availableVoices() }

            coVerify(exactly = 1) { engineProvider() }
            verify(exactly = 1) { engine.voices }
            confirmVerified(engineProvider, engine, defaultVoiceIdProvider)
        }

    @Test
    fun `再読み込み時は一覧を再取得する`() =
        runTest {
            coEvery { engineProvider() } returns engine
            every { defaultVoiceIdProvider() } returns null
            every { engine.voices } returnsMany listOf(null, emptySet())

            assertEquals(emptyList(), repository.availableVoices())
            assertEquals(emptyList(), repository.availableVoices())

            coVerify(exactly = 2) { engineProvider() }
            verify(exactly = 2) { engine.voices }
            verify(exactly = 2) { defaultVoiceIdProvider() }
            confirmVerified(engineProvider, engine, defaultVoiceIdProvider)
        }
}
