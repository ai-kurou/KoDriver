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
import kurou.kodriver.core.narrator.NarratorEngine
import kurou.kodriver.core.narrator.SoundPlayer
import kurou.kodriver.core.narrator.WavResources
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsNarratorEngineTest {
    private val soundPlayer: SoundPlayer = mockk()
    private val wavNarratorEngine: NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey> = mockk()

    @Test
    fun `currentReadoutItemKeyはNarratorEngineのcurrentKeyを返す`() {
        every { wavNarratorEngine.currentKey } returns LmuWindowsReadoutItemKey.VehicleApproach.Root
        val engine = LmuWindowsNarratorEngine(wavNarratorEngine)

        assertEquals(LmuWindowsReadoutItemKey.VehicleApproach.Root, engine.currentReadoutItemKey)

        verify(exactly = 1) { wavNarratorEngine.currentKey }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `speakはNarratorEngineのspeakへ委譲する`() {
        every { wavNarratorEngine.speak(SpeechEvent.LmuWindowsCarLeft(), queue = true) } just Runs
        val engine = LmuWindowsNarratorEngine(wavNarratorEngine)

        engine.speak(SpeechEvent.LmuWindowsCarLeft(), queue = true)

        verify(exactly = 1) { wavNarratorEngine.speak(SpeechEvent.LmuWindowsCarLeft(), queue = true) }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `stopはNarratorEngineのstopへ委譲する`() {
        every { wavNarratorEngine.stop() } just Runs
        val engine = LmuWindowsNarratorEngine(wavNarratorEngine)

        engine.stop()

        verify(exactly = 1) { wavNarratorEngine.stop() }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `previewStartSoundはNarratorEngineのpreviewStartSoundへ委譲する`() {
        every { wavNarratorEngine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO) } just Runs
        val engine = LmuWindowsNarratorEngine(wavNarratorEngine)

        engine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO)

        verify(exactly = 1) { wavNarratorEngine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO) }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `playStartSoundはNarratorEngineのplayStartSoundForKeyへ委譲する`() =
        runTest {
            coEvery {
                wavNarratorEngine.playStartSoundForKey(LmuWindowsReadoutItemKey.Flag.SectorYellowFlag)
            } just Runs
            val engine = LmuWindowsNarratorEngine(wavNarratorEngine)

            engine.playStartSound(LmuWindowsReadoutItemKey.Flag.SectorYellowFlag)

            coVerify(exactly = 1) {
                wavNarratorEngine.playStartSoundForKey(LmuWindowsReadoutItemKey.Flag.SectorYellowFlag)
            }
            confirmVerified(wavNarratorEngine)
        }

    @Test
    fun `残量警告は閾値が異なってもカスタム読み上げ対象でWAVにフォールバックしない`() =
        runTest {
            val customEvents = mutableListOf<SpeechEvent>()
            val engine =
                NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey>(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<ReadoutStartSoundType>(
                            startSoundTypeToFile = emptyMap(),
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = {
                        it is SpeechEvent.LmuWindowsPitTimingWarning ||
                            it is SpeechEvent.LmuWindowsRemainingVirtualEnergyWarning
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(SpeechEvent.LmuWindowsRemainingVirtualEnergyWarning(30))
            runCurrent()
            narrator.speak(SpeechEvent.LmuWindowsRemainingVirtualEnergyWarning(70))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.LmuWindowsRemainingVirtualEnergyWarning(30),
                    SpeechEvent.LmuWindowsRemainingVirtualEnergyWarning(70),
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
                NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey>(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<ReadoutStartSoundType>(
                            startSoundTypeToFile = emptyMap(),
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = {
                        it is SpeechEvent.LmuWindowsPitTimingWarning || it is SpeechEvent.LmuWindowsTyreWearWarning
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(SpeechEvent.LmuWindowsTyreWearWarning(30))
            runCurrent()
            narrator.speak(SpeechEvent.LmuWindowsTyreWearWarning(70))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.LmuWindowsTyreWearWarning(30),
                    SpeechEvent.LmuWindowsTyreWearWarning(70),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }

    @Test
    fun `ブレーキ過熱警告は閾値が異なってもカスタム読み上げ対象でWAVにフォールバックしない`() =
        runTest {
            val customEvents = mutableListOf<SpeechEvent>()
            val engine =
                NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey>(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<ReadoutStartSoundType>(
                            startSoundTypeToFile = emptyMap(),
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = {
                        it is SpeechEvent.LmuWindowsPitTimingWarning || it is SpeechEvent.LmuWindowsBrakeOverheat
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(SpeechEvent.LmuWindowsBrakeOverheat(700))
            runCurrent()
            narrator.speak(SpeechEvent.LmuWindowsBrakeOverheat(900))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.LmuWindowsBrakeOverheat(700),
                    SpeechEvent.LmuWindowsBrakeOverheat(900),
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
                NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey>(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<ReadoutStartSoundType>(
                            startSoundTypeToFile = emptyMap(),
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = { it is SpeechEvent.LmuWindowsTyreOverheat },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(SpeechEvent.LmuWindowsTyreOverheat(100))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.LmuWindowsTyreOverheat(100),
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
                NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey>(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<ReadoutStartSoundType>(
                            startSoundTypeToFile = emptyMap(),
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = { it is SpeechEvent.LmuWindowsTyreCold },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(SpeechEvent.LmuWindowsTyreCold(60))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.LmuWindowsTyreCold(60),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }

    @Test
    fun `オーバーヒートは解決済み文言が異なってもカスタム読み上げ対象でWAVにフォールバックしない`() =
        runTest {
            val customEvents = mutableListOf<SpeechEvent>()
            val engine =
                NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey>(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<ReadoutStartSoundType>(
                            startSoundTypeToFile = emptyMap(),
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = {
                        it is SpeechEvent.LmuWindowsPitTimingWarning || it is SpeechEvent.LmuWindowsOverheating
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(SpeechEvent.LmuWindowsOverheating())
            runCurrent()
            narrator.speak(SpeechEvent.LmuWindowsOverheating("カスタム"))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.LmuWindowsOverheating(),
                    SpeechEvent.LmuWindowsOverheating("カスタム"),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }

    @Test
    fun `自己ベストラップは解決済み文言が異なってもカスタム読み上げ対象でWAVにフォールバックしない`() =
        runTest {
            val customEvents = mutableListOf<SpeechEvent>()
            val engine =
                NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey>(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<ReadoutStartSoundType>(
                            startSoundTypeToFile = emptyMap(),
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = {
                        it is SpeechEvent.LmuWindowsPitTimingWarning || it is SpeechEvent.LmuWindowsMyBestLap
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(SpeechEvent.LmuWindowsMyBestLap(83_456L))
            runCurrent()
            narrator.speak(SpeechEvent.LmuWindowsMyBestLap(23_005L, "カスタム"))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.LmuWindowsMyBestLap(83_456L),
                    SpeechEvent.LmuWindowsMyBestLap(23_005L, "カスタム"),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }

    @Test
    fun `部品脱落は解決済み文言が異なってもカスタム読み上げ対象でWAVにフォールバックしない`() =
        runTest {
            val customEvents = mutableListOf<SpeechEvent>()
            val engine =
                NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey>(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<ReadoutStartSoundType>(
                            startSoundTypeToFile = emptyMap(),
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = {
                        it is SpeechEvent.LmuWindowsPitTimingWarning || it is SpeechEvent.LmuWindowsPartDetached
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(SpeechEvent.LmuWindowsPartDetached())
            runCurrent()
            narrator.speak(SpeechEvent.LmuWindowsPartDetached("カスタム"))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.LmuWindowsPartDetached(),
                    SpeechEvent.LmuWindowsPartDetached("カスタム"),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }

    @Test
    fun `タイヤ脱落は解決済み文言が異なってもカスタム読み上げ対象でWAVにフォールバックしない`() =
        runTest {
            val customEvents = mutableListOf<SpeechEvent>()
            val engine =
                NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey>(
                    soundPlayer = soundPlayer,
                    resources =
                        WavResources<ReadoutStartSoundType>(
                            startSoundTypeToFile = emptyMap(),
                            startSoundResourceLoader = { error("開始音は設定しない") },
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    isCustomSpeakEvent = {
                        it is SpeechEvent.LmuWindowsPitTimingWarning || it is SpeechEvent.LmuWindowsTyreDetached
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(SpeechEvent.LmuWindowsTyreDetached())
            runCurrent()
            narrator.speak(SpeechEvent.LmuWindowsTyreDetached("カスタム"))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.LmuWindowsTyreDetached(),
                    SpeechEvent.LmuWindowsTyreDetached("カスタム"),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }
}
