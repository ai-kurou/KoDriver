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
import kurou.kodriver.core.narrator.NarratorEngine
import kurou.kodriver.core.narrator.SoundPlayer
import kurou.kodriver.core.narrator.WavResources
import kurou.kodriver.domain.engine.Gt7Ps5MyBestLap
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelLapsWarning
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelWarning
import kurou.kodriver.domain.engine.Gt7Ps5TyreOverheat
import kurou.kodriver.domain.engine.LmuWindowsCarLeft
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5NarratorEngineTest {
    private val soundPlayer: SoundPlayer = mockk()
    private val wavNarratorEngine: NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey> = mockk()

    @Test
    fun `currentReadoutItemKeyはNarratorEngineのcurrentKeyを返す`() {
        every { wavNarratorEngine.currentKey } returns Gt7Ps5ReadoutItemKey.MyBestLap.Root
        val engine = Gt7Ps5NarratorEngine(wavNarratorEngine)

        assertEquals(Gt7Ps5ReadoutItemKey.MyBestLap.Root, engine.currentReadoutItemKey)

        verify(exactly = 1) { wavNarratorEngine.currentKey }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `speakはNarratorEngineのspeakへ委譲する`() {
        every { wavNarratorEngine.speak(LmuWindowsCarLeft(), queue = true) } just Runs
        val engine = Gt7Ps5NarratorEngine(wavNarratorEngine)

        engine.speak(LmuWindowsCarLeft(), queue = true)

        verify(exactly = 1) { wavNarratorEngine.speak(LmuWindowsCarLeft(), queue = true) }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `stopはNarratorEngineのstopへ委譲する`() {
        every { wavNarratorEngine.stop() } just Runs
        val engine = Gt7Ps5NarratorEngine(wavNarratorEngine)

        engine.stop()

        verify(exactly = 1) { wavNarratorEngine.stop() }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `previewStartSoundはNarratorEngineのpreviewStartSoundへ委譲する`() {
        every { wavNarratorEngine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO) } just Runs
        val engine = Gt7Ps5NarratorEngine(wavNarratorEngine)

        engine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO)

        verify(exactly = 1) { wavNarratorEngine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO) }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `playStartSoundはNarratorEngineのplayStartSoundForKeyへ委譲する`() =
        runTest {
            coEvery { wavNarratorEngine.playStartSoundForKey(Gt7Ps5ReadoutItemKey.MyBestLap.Root) } just Runs
            val engine = Gt7Ps5NarratorEngine(wavNarratorEngine)

            engine.playStartSound(Gt7Ps5ReadoutItemKey.MyBestLap.Root)

            coVerify(exactly = 1) { wavNarratorEngine.playStartSoundForKey(Gt7Ps5ReadoutItemKey.MyBestLap.Root) }
            confirmVerified(wavNarratorEngine)
        }

    @Test
    fun `燃料残り周回数は値が異なってもカスタム読み上げ対象でWAVにフォールバックしない`() =
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
                        it is Gt7Ps5RemainingFuelLapsWarning
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = Gt7Ps5NarratorEngine(engine)
            narrator.speak(Gt7Ps5RemainingFuelLapsWarning(3, "あと3周"))
            runCurrent()
            narrator.speak(Gt7Ps5RemainingFuelLapsWarning(0, "燃料なし"))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    Gt7Ps5RemainingFuelLapsWarning(3, "あと3周"),
                    Gt7Ps5RemainingFuelLapsWarning(0, "燃料なし"),
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
                        it is Gt7Ps5RemainingFuelWarning
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = Gt7Ps5NarratorEngine(engine)
            narrator.speak(Gt7Ps5RemainingFuelWarning(30, "あと30%"))
            runCurrent()
            narrator.speak(Gt7Ps5RemainingFuelWarning(10, "あと10%"))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    Gt7Ps5RemainingFuelWarning(30, "あと30%"),
                    Gt7Ps5RemainingFuelWarning(10, "あと10%"),
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
                        it is Gt7Ps5TyreOverheat
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = Gt7Ps5NarratorEngine(engine)
            narrator.speak(Gt7Ps5TyreOverheat(107, "タイヤ107度"))
            runCurrent()
            narrator.speak(Gt7Ps5TyreOverheat(95, "タイヤ95度"))
            runCurrent()
            narrator.speak(Gt7Ps5TyreOverheat(95, ""))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    Gt7Ps5TyreOverheat(107, "タイヤ107度"),
                    Gt7Ps5TyreOverheat(95, "タイヤ95度"),
                    Gt7Ps5TyreOverheat(95, ""),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }

    @Test
    fun `自己ベストラップ更新は値が異なってもカスタム読み上げ対象でWAVにフォールバックしない`() =
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
                        it is Gt7Ps5MyBestLap
                    },
                    customSpeak = { event, _ -> customEvents += event },
                    scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                )
            runCurrent()
            val narrator = Gt7Ps5NarratorEngine(engine)
            narrator.speak(Gt7Ps5MyBestLap(83_456, "更新1分23秒456"))
            runCurrent()
            narrator.speak(Gt7Ps5MyBestLap(59_000, "更新59秒000"))
            runCurrent()
            narrator.speak(Gt7Ps5MyBestLap(59_000, ""))
            runCurrent()
            assertEquals(
                listOf<SpeechEvent>(
                    Gt7Ps5MyBestLap(83_456, "更新1分23秒456"),
                    Gt7Ps5MyBestLap(59_000, "更新59秒000"),
                    Gt7Ps5MyBestLap(59_000, ""),
                ),
                customEvents,
            )
            coVerify(exactly = 0) { soundPlayer.play(byteArrayOf(1), 100) }
            confirmVerified(soundPlayer)
        }
}
