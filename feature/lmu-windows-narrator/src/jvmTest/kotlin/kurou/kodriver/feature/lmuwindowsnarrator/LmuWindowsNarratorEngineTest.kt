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
import kurou.kodriver.domain.engine.LmuWindowsBrakeOverheat
import kurou.kodriver.domain.engine.LmuWindowsCarLeft
import kurou.kodriver.domain.engine.LmuWindowsMyBestLap
import kurou.kodriver.domain.engine.LmuWindowsOverheating
import kurou.kodriver.domain.engine.LmuWindowsPartDetached
import kurou.kodriver.domain.engine.LmuWindowsPitTimingWarning
import kurou.kodriver.domain.engine.LmuWindowsRemainingVirtualEnergyWarning
import kurou.kodriver.domain.engine.LmuWindowsTyreCold
import kurou.kodriver.domain.engine.LmuWindowsTyreDetached
import kurou.kodriver.domain.engine.LmuWindowsTyreOverheat
import kurou.kodriver.domain.engine.LmuWindowsTyreWearWarning
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
        every { wavNarratorEngine.speak(LmuWindowsCarLeft(), queue = true) } just Runs
        val engine = LmuWindowsNarratorEngine(wavNarratorEngine)

        engine.speak(LmuWindowsCarLeft(), queue = true)

        verify(exactly = 1) { wavNarratorEngine.speak(LmuWindowsCarLeft(), queue = true) }
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
                        it is LmuWindowsPitTimingWarning ||
                            it is LmuWindowsRemainingVirtualEnergyWarning
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(LmuWindowsRemainingVirtualEnergyWarning(30))
            runCurrent()
            narrator.speak(LmuWindowsRemainingVirtualEnergyWarning(70))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsRemainingVirtualEnergyWarning(30),
                    LmuWindowsRemainingVirtualEnergyWarning(70),
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
                        it is LmuWindowsPitTimingWarning || it is LmuWindowsTyreWearWarning
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(LmuWindowsTyreWearWarning(30))
            runCurrent()
            narrator.speak(LmuWindowsTyreWearWarning(70))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsTyreWearWarning(30),
                    LmuWindowsTyreWearWarning(70),
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
                        it is LmuWindowsPitTimingWarning || it is LmuWindowsBrakeOverheat
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(LmuWindowsBrakeOverheat(700))
            runCurrent()
            narrator.speak(LmuWindowsBrakeOverheat(900))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsBrakeOverheat(700),
                    LmuWindowsBrakeOverheat(900),
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
                    isCustomSpeakEvent = { it is LmuWindowsTyreOverheat },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(LmuWindowsTyreOverheat(100))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsTyreOverheat(100),
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
                    isCustomSpeakEvent = { it is LmuWindowsTyreCold },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(LmuWindowsTyreCold(60))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsTyreCold(60),
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
                        it is LmuWindowsPitTimingWarning || it is LmuWindowsOverheating
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(LmuWindowsOverheating())
            runCurrent()
            narrator.speak(LmuWindowsOverheating("カスタム"))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsOverheating(),
                    LmuWindowsOverheating("カスタム"),
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
                        it is LmuWindowsPitTimingWarning || it is LmuWindowsMyBestLap
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(LmuWindowsMyBestLap(83_456L))
            runCurrent()
            narrator.speak(LmuWindowsMyBestLap(23_005L, "カスタム"))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsMyBestLap(83_456L),
                    LmuWindowsMyBestLap(23_005L, "カスタム"),
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
                        it is LmuWindowsPitTimingWarning || it is LmuWindowsPartDetached
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(LmuWindowsPartDetached())
            runCurrent()
            narrator.speak(LmuWindowsPartDetached("カスタム"))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsPartDetached(),
                    LmuWindowsPartDetached("カスタム"),
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
                        it is LmuWindowsPitTimingWarning || it is LmuWindowsTyreDetached
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = LmuWindowsNarratorEngine(engine)
            narrator.speak(LmuWindowsTyreDetached())
            runCurrent()
            narrator.speak(LmuWindowsTyreDetached("カスタム"))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsTyreDetached(),
                    LmuWindowsTyreDetached("カスタム"),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }
}
