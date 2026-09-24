@file:Suppress("FunctionNaming")

package kurou.kodriver.core.texttospeechdata.repository

import android.speech.tts.TextToSpeech
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidTextToSpeechRepositoryTest {
    private val textToSpeech: TextToSpeech = mockk(relaxUnitFun = true)
    private var factoryCallCount = 0

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
            every {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_ADD, null, "kodriver_tts")
            } returns TextToSpeech.SUCCESS
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            repository.speak("ベストラップ", queue = true)

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) {
                textToSpeech.speak("ベストラップ", TextToSpeech.QUEUE_ADD, null, "kodriver_tts")
            }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `初期化は最初の1回のみ行う`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_AVAILABLE
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            assertTrue(repository.isAvailable())
            assertTrue(repository.isAvailable())

            assertEquals(1, factoryCallCount)
        }

    @Test
    fun `読み上げ言語が利用できない場合は利用不可としてエンジンを破棄する`() =
        runTest {
            every { textToSpeech.setLanguage(Locale.JAPANESE) } returns TextToSpeech.LANG_NOT_SUPPORTED
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            assertFalse(repository.isAvailable())
            repository.speak("ベストラップ", queue = false)

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.shutdown() }
            confirmVerified(textToSpeech)
        }

    @Test
    fun `初期化に失敗した場合は利用不可としてエンジンを破棄する`() =
        runTest {
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.ERROR))

            assertFalse(repository.isAvailable())

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
            every { textToSpeech.stop() } returns TextToSpeech.SUCCESS
            val repository = AndroidTextToSpeechRepository(factory(TextToSpeech.SUCCESS))

            repository.stop()
            assertEquals(0, factoryCallCount)

            assertTrue(repository.isAvailable())
            repository.stop()

            verify(exactly = 1) { textToSpeech.setLanguage(Locale.JAPANESE) }
            verify(exactly = 1) { textToSpeech.stop() }
            confirmVerified(textToSpeech)
        }
}
