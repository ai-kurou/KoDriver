package kurou.kodriver.feature.gt7ps5narrator

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kurou.kodriver.core.narrator.WavNarratorEngine
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5WavNarratorEngineTest {
    @MockK
    private lateinit var wavNarratorEngine: WavNarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey>

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `currentReadoutItemKeyはWavNarratorEngineのcurrentKeyを返す`() {
        every { wavNarratorEngine.currentKey } returns ReadoutItemKey.Gt7Ps5.MyBestLap.Root
        val engine = Gt7Ps5WavNarratorEngine(wavNarratorEngine)

        assertEquals(ReadoutItemKey.Gt7Ps5.MyBestLap.Root, engine.currentReadoutItemKey)

        verify(exactly = 1) { wavNarratorEngine.currentKey }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `speakはWavNarratorEngineのspeakへ委譲する`() {
        every { wavNarratorEngine.speak(SpeechEvent.CarLeft, queue = true) } just Runs
        val engine = Gt7Ps5WavNarratorEngine(wavNarratorEngine)

        engine.speak(SpeechEvent.CarLeft, queue = true)

        verify(exactly = 1) { wavNarratorEngine.speak(SpeechEvent.CarLeft, queue = true) }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `stopはWavNarratorEngineのstopへ委譲する`() {
        every { wavNarratorEngine.stop() } just Runs
        val engine = Gt7Ps5WavNarratorEngine(wavNarratorEngine)

        engine.stop()

        verify(exactly = 1) { wavNarratorEngine.stop() }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `previewStartSoundはWavNarratorEngineのpreviewStartSoundへ委譲する`() {
        every { wavNarratorEngine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO) } just Runs
        val engine = Gt7Ps5WavNarratorEngine(wavNarratorEngine)

        engine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO)

        verify(exactly = 1) { wavNarratorEngine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO) }
        confirmVerified(wavNarratorEngine)
    }
}
