@file:Suppress("FunctionNaming")

package kurou.kodriver.core.speechrecognitiondata.speech

import android.content.Context
import android.content.res.AssetManager
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.vosk.Recognizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidVoskSpeechTranscriberTest {
    private val context: Context = mockk()
    private val assets: AssetManager = mockk()

    @Test
    fun `モデルがassetsにあれば利用できる`() {
        every { context.assets } returns assets
        every { assets.list(VOSK_MODEL_ASSET_DIRECTORY) } returns arrayOf("am")

        assertTrue(AndroidVoskSpeechTranscriberFactory(context).isAvailable())

        verify(exactly = 1) { context.assets }
        verify(exactly = 1) { assets.list(VOSK_MODEL_ASSET_DIRECTORY) }
        confirmVerified(context, assets)
    }

    @Test
    fun `モデルがassetsに無ければ利用できない`() {
        every { context.assets } returns assets
        every { assets.list(VOSK_MODEL_ASSET_DIRECTORY) } returns emptyArray()

        assertFalse(AndroidVoskSpeechTranscriberFactory(context).isAvailable())
    }

    @Test
    fun `モデルのリストが取れなければ利用できない`() {
        every { context.assets } returns assets
        every { assets.list(VOSK_MODEL_ASSET_DIRECTORY) } returns null

        assertFalse(AndroidVoskSpeechTranscriberFactory(context).isAvailable())
    }

    @Test
    fun `アダプターはRecognizerの各操作へ委譲する`() {
        val recognizer = mockk<Recognizer>()
        every { recognizer.setMaxAlternatives(3) } returns Unit
        every { recognizer.result } returns "result"
        every { recognizer.partialResult } returns "partial"
        every { recognizer.reset() } returns Unit
        every { recognizer.close() } returns Unit
        val pcm = ByteArray(2)
        every { recognizer.acceptWaveForm(pcm, 2) } returns true
        val adapter = VoskRecognizerAdapter(recognizer)

        adapter.setMaxAlternatives(3)
        assertTrue(adapter.acceptWaveForm(pcm, 2))
        assertEquals("result", adapter.result())
        assertEquals("partial", adapter.partialResult())
        adapter.reset()
        adapter.close()

        verify(exactly = 1) { recognizer.setMaxAlternatives(3) }
        verify(exactly = 1) { recognizer.acceptWaveForm(pcm, 2) }
        verify(exactly = 1) { recognizer.result }
        verify(exactly = 1) { recognizer.partialResult }
        verify(exactly = 1) { recognizer.reset() }
        verify(exactly = 1) { recognizer.close() }
        confirmVerified(recognizer)
    }
}
