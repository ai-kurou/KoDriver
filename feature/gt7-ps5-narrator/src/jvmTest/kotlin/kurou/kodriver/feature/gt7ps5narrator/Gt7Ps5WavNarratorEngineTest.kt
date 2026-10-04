package kurou.kodriver.feature.gt7ps5narrator

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.narrator.SoundPlayer
import kurou.kodriver.core.narrator.WavNarratorEngine
import kurou.kodriver.core.narrator.WavResources
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5WavNarratorEngineTest {
    private val soundPlayer: SoundPlayer = mockk()
    private val wavNarratorEngine: WavNarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey> = mockk()

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

    @Test
    fun `playStartSoundはWavNarratorEngineのplayStartSoundForKeyへ委譲する`() =
        runTest {
            coEvery { wavNarratorEngine.playStartSoundForKey(ReadoutItemKey.Gt7Ps5.MyBestLap.Root) } just Runs
            val engine = Gt7Ps5WavNarratorEngine(wavNarratorEngine)

            engine.playStartSound(ReadoutItemKey.Gt7Ps5.MyBestLap.Root)

            coVerify(exactly = 1) { wavNarratorEngine.playStartSoundForKey(ReadoutItemKey.Gt7Ps5.MyBestLap.Root) }
            confirmVerified(wavNarratorEngine)
        }

    @Test
    fun `燃料残り周回数は値が異なってもカスタム読み上げ対象でWAVにフォールバックしない`() =
        runTest {
            val customEvents = mutableListOf<SpeechEvent>()
            val engine =
                WavNarratorEngine(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<SpeechEvent, ReadoutStartSoundType>(
                            eventToFile = mapOf(SpeechEvent.RemainingFuelLapsWarning(3, "あと3周") to "warning.wav"),
                            startSoundTypeToFile = emptyMap(),
                            resourceLoader = { byteArrayOf(1) },
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = {
                        it is SpeechEvent.RemainingFuelLapsWarning
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = Gt7Ps5WavNarratorEngine(engine)
            narrator.speak(SpeechEvent.RemainingFuelLapsWarning(3, "あと3周"))
            runCurrent()
            narrator.speak(SpeechEvent.RemainingFuelLapsWarning(0, "燃料なし"))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.RemainingFuelLapsWarning(3, "あと3周"),
                    SpeechEvent.RemainingFuelLapsWarning(0, "燃料なし"),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }

    @Test
    fun `燃料残量は値が異なってもカスタム読み上げ対象でWAVにフォールバックしない`() =
        runTest {
            val customEvents = mutableListOf<SpeechEvent>()
            val engine =
                WavNarratorEngine(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<SpeechEvent, ReadoutStartSoundType>(
                            eventToFile = mapOf(SpeechEvent.Gt7Ps5RemainingFuelWarning(30, "あと30%") to "warning.wav"),
                            startSoundTypeToFile = emptyMap(),
                            resourceLoader = { byteArrayOf(1) },
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = {
                        it is SpeechEvent.Gt7Ps5RemainingFuelWarning
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = Gt7Ps5WavNarratorEngine(engine)
            narrator.speak(SpeechEvent.Gt7Ps5RemainingFuelWarning(30, "あと30%"))
            runCurrent()
            narrator.speak(SpeechEvent.Gt7Ps5RemainingFuelWarning(10, "あと10%"))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.Gt7Ps5RemainingFuelWarning(30, "あと30%"),
                    SpeechEvent.Gt7Ps5RemainingFuelWarning(10, "あと10%"),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }

    @Test
    fun `タイヤ過熱は値が異なってもカスタム読み上げ対象でWAVにフォールバックしない`() =
        runTest {
            val customEvents = mutableListOf<SpeechEvent>()
            val engine =
                WavNarratorEngine(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<SpeechEvent, ReadoutStartSoundType>(
                            eventToFile = mapOf(SpeechEvent.Gt7Ps5TyreOverheat(107, "タイヤ107度") to "warning.wav"),
                            startSoundTypeToFile = emptyMap(),
                            resourceLoader = { byteArrayOf(1) },
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = {
                        it is SpeechEvent.Gt7Ps5TyreOverheat
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = Gt7Ps5WavNarratorEngine(engine)
            narrator.speak(SpeechEvent.Gt7Ps5TyreOverheat(107, "タイヤ107度"))
            runCurrent()
            narrator.speak(SpeechEvent.Gt7Ps5TyreOverheat(95, "タイヤ95度"))
            runCurrent()
            narrator.speak(SpeechEvent.Gt7Ps5TyreOverheat(95, ""))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.Gt7Ps5TyreOverheat(107, "タイヤ107度"),
                    SpeechEvent.Gt7Ps5TyreOverheat(95, "タイヤ95度"),
                    SpeechEvent.Gt7Ps5TyreOverheat(95, ""),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }
}
