package kurou.kodriver.feature.lmuwindowsnarrator

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
class LmuWindowsWavNarratorEngineTest {
    private val soundPlayer: SoundPlayer = mockk()
    private val wavNarratorEngine: WavNarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey> = mockk()

    @Test
    fun `currentReadoutItemKeyはWavNarratorEngineのcurrentKeyを返す`() {
        every { wavNarratorEngine.currentKey } returns ReadoutItemKey.LmuWindows.VehicleApproach.Root
        val engine = LmuWindowsWavNarratorEngine(wavNarratorEngine)

        assertEquals(ReadoutItemKey.LmuWindows.VehicleApproach.Root, engine.currentReadoutItemKey)

        verify(exactly = 1) { wavNarratorEngine.currentKey }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `speakはWavNarratorEngineのspeakへ委譲する`() {
        every { wavNarratorEngine.speak(SpeechEvent.CarLeft, queue = true) } just Runs
        val engine = LmuWindowsWavNarratorEngine(wavNarratorEngine)

        engine.speak(SpeechEvent.CarLeft, queue = true)

        verify(exactly = 1) { wavNarratorEngine.speak(SpeechEvent.CarLeft, queue = true) }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `stopはWavNarratorEngineのstopへ委譲する`() {
        every { wavNarratorEngine.stop() } just Runs
        val engine = LmuWindowsWavNarratorEngine(wavNarratorEngine)

        engine.stop()

        verify(exactly = 1) { wavNarratorEngine.stop() }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `previewStartSoundはWavNarratorEngineのpreviewStartSoundへ委譲する`() {
        every { wavNarratorEngine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO) } just Runs
        val engine = LmuWindowsWavNarratorEngine(wavNarratorEngine)

        engine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO)

        verify(exactly = 1) { wavNarratorEngine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO) }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `playStartSoundはWavNarratorEngineのplayStartSoundForKeyへ委譲する`() =
        runTest {
            coEvery {
                wavNarratorEngine.playStartSoundForKey(ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag)
            } just Runs
            val engine = LmuWindowsWavNarratorEngine(wavNarratorEngine)

            engine.playStartSound(ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag)

            coVerify(exactly = 1) {
                wavNarratorEngine.playStartSoundForKey(ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag)
            }
            confirmVerified(wavNarratorEngine)
        }

    @Test
    fun `残量警告は閾値が異なってもカスタム読み上げ対象でWAVにフォールバックしない`() =
        runTest {
            val customEvents = mutableListOf<SpeechEvent>()
            val engine =
                WavNarratorEngine(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<SpeechEvent, ReadoutStartSoundType>(
                            eventToFile = mapOf(SpeechEvent.RemainingVirtualEnergyWarning(30) to "warning.wav"),
                            startSoundTypeToFile = emptyMap(),
                            resourceLoader = { byteArrayOf(1) },
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = {
                        it is SpeechEvent.PitTimingWarning || it is SpeechEvent.RemainingVirtualEnergyWarning
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsWavNarratorEngine(engine)
            narrator.speak(SpeechEvent.RemainingVirtualEnergyWarning(30))
            runCurrent()
            narrator.speak(SpeechEvent.RemainingVirtualEnergyWarning(70))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.RemainingVirtualEnergyWarning(30),
                    SpeechEvent.RemainingVirtualEnergyWarning(70),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }

    @Test
    fun `摩耗警告は閾値が異なってもカスタム読み上げ対象でWAVにフォールバックしない`() =
        runTest {
            val customEvents = mutableListOf<SpeechEvent>()
            val engine =
                WavNarratorEngine(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<SpeechEvent, ReadoutStartSoundType>(
                            eventToFile = mapOf(SpeechEvent.TyreWearWarning(30) to "warning.wav"),
                            startSoundTypeToFile = emptyMap(),
                            resourceLoader = { byteArrayOf(1) },
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = {
                        it is SpeechEvent.PitTimingWarning || it is SpeechEvent.TyreWearWarning
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsWavNarratorEngine(engine)
            narrator.speak(SpeechEvent.TyreWearWarning(30))
            runCurrent()
            narrator.speak(SpeechEvent.TyreWearWarning(70))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.TyreWearWarning(30),
                    SpeechEvent.TyreWearWarning(70),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }

    @Test
    fun `過熱警告はカスタム読み上げへ渡しWAVにフォールバックしない`() =
        runTest {
            val customEvents = mutableListOf<SpeechEvent>()
            val engine =
                WavNarratorEngine(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<SpeechEvent, ReadoutStartSoundType>(
                            eventToFile = mapOf(SpeechEvent.TyreOverheat(100) to "warning.wav"),
                            startSoundTypeToFile = emptyMap(),
                            resourceLoader = { byteArrayOf(1) },
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = { it is SpeechEvent.TyreOverheat },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsWavNarratorEngine(engine)
            narrator.speak(SpeechEvent.TyreOverheat(100))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.TyreOverheat(100),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }

    @Test
    fun `低温警告はカスタム読み上げへ渡しWAVにフォールバックしない`() =
        runTest {
            val customEvents = mutableListOf<SpeechEvent>()
            val engine =
                WavNarratorEngine(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<SpeechEvent, ReadoutStartSoundType>(
                            eventToFile = mapOf(SpeechEvent.TyreCold(60) to "warning.wav"),
                            startSoundTypeToFile = emptyMap(),
                            resourceLoader = { byteArrayOf(1) },
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = { it is SpeechEvent.TyreCold },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsWavNarratorEngine(engine)
            narrator.speak(SpeechEvent.TyreCold(60))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.TyreCold(60),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }
}
