package kurou.kodriver.feature.acewindowsnarrator

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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.narrator.NarratorEngine
import kurou.kodriver.core.narrator.SoundPlayer
import kurou.kodriver.core.narrator.WavResources
import kurou.kodriver.domain.engine.AceWindowsBlackFlag
import kurou.kodriver.domain.engine.AceWindowsBlackWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsBlueFlag
import kurou.kodriver.domain.engine.AceWindowsCheckeredFlag
import kurou.kodriver.domain.engine.AceWindowsGreenFlag
import kurou.kodriver.domain.engine.AceWindowsOrangeCircleFlag
import kurou.kodriver.domain.engine.AceWindowsRedFlag
import kurou.kodriver.domain.engine.AceWindowsRedYellowStripesFlag
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelLapsWarning
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelWarning
import kurou.kodriver.domain.engine.AceWindowsTyreOverheat
import kurou.kodriver.domain.engine.AceWindowsVehicleApproach
import kurou.kodriver.domain.engine.AceWindowsWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsYellowFlag
import kurou.kodriver.domain.engine.LmuWindowsCarLeft
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsNarratorEngineTest {
    private val soundPlayer: SoundPlayer = mockk()
    private val wavNarratorEngine: NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey> = mockk()

    @Test
    fun `currentReadoutItemKeyはNarratorEngineのcurrentKeyを返す`() {
        every { wavNarratorEngine.currentKey } returns AceWindowsReadoutItemKey.VehicleApproach.Root
        val engine = AceWindowsNarratorEngine(wavNarratorEngine)

        assertEquals(AceWindowsReadoutItemKey.VehicleApproach.Root, engine.currentReadoutItemKey)

        verify(exactly = 1) { wavNarratorEngine.currentKey }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `speakはNarratorEngineのspeakへ委譲する`() {
        every { wavNarratorEngine.speak(LmuWindowsCarLeft(), queue = true) } just Runs
        val engine = AceWindowsNarratorEngine(wavNarratorEngine)

        engine.speak(LmuWindowsCarLeft(), queue = true)

        verify(exactly = 1) { wavNarratorEngine.speak(LmuWindowsCarLeft(), queue = true) }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `stopはNarratorEngineのstopへ委譲する`() {
        every { wavNarratorEngine.stop() } just Runs
        val engine = AceWindowsNarratorEngine(wavNarratorEngine)

        engine.stop()

        verify(exactly = 1) { wavNarratorEngine.stop() }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `previewStartSoundはNarratorEngineのpreviewStartSoundへ委譲する`() {
        every { wavNarratorEngine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO) } just Runs
        val engine = AceWindowsNarratorEngine(wavNarratorEngine)

        engine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO)

        verify(exactly = 1) { wavNarratorEngine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO) }
        confirmVerified(wavNarratorEngine)
    }

    @Test
    fun `playStartSoundはNarratorEngineのplayStartSoundForKeyへ委譲する`() =
        runTest {
            coEvery {
                wavNarratorEngine.playStartSoundForKey(AceWindowsReadoutItemKey.VehicleApproach.Root)
            } just Runs
            val engine = AceWindowsNarratorEngine(wavNarratorEngine)

            engine.playStartSound(AceWindowsReadoutItemKey.VehicleApproach.Root)

            coVerify(exactly = 1) {
                wavNarratorEngine.playStartSoundForKey(AceWindowsReadoutItemKey.VehicleApproach.Root)
            }
            confirmVerified(wavNarratorEngine)
        }

    @Test
    fun `フラッグと車両接近とタイヤ過熱と燃料残量と残り周回数はRoot開始音の後にTTS本文を再生しWAVにフォールバックしない`() =
        runTest {
            val events =
                listOf(
                    AceWindowsVehicleApproach(),
                    AceWindowsRemainingFuelWarning(20, "燃料は残り20パーセント"),
                    AceWindowsRemainingFuelLapsWarning(2, "残り2周"),
                    AceWindowsRemainingFuelLapsWarning(0, "燃料なし"),
                    AceWindowsTyreOverheat(110, "タイヤ過熱 110度"),
                    AceWindowsCheckeredFlag(),
                    AceWindowsWhiteFlag(),
                    AceWindowsGreenFlag(),
                    AceWindowsRedFlag(),
                    AceWindowsBlueFlag(),
                    AceWindowsYellowFlag(),
                    AceWindowsBlackFlag(),
                    AceWindowsBlackWhiteFlag(),
                    AceWindowsOrangeCircleFlag(),
                    AceWindowsRedYellowStripesFlag(),
                )
            val startSound = byteArrayOf(2)
            events.forEachIndexed { index, target ->
                val calls = mutableListOf<String>()
                coEvery { soundPlayer.play(startSound, 42) } answers { calls += "start" }
                val engine =
                    NarratorEngine<SpeechEvent, ReadoutStartSoundType, ReadoutItemKey>(
                        soundPlayer = soundPlayer,
                        resources =
                            WavResources<ReadoutStartSoundType>(
                                startSoundTypeToFile = mapOf(ReadoutStartSoundType.FORMULA_RADIO to "start.wav"),
                                startSoundResourceLoader = { startSound },
                            ),
                        eventToKey = { it.readoutItemKey },
                        defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                        volumeFlow = flowOf(42),
                        startSoundEnabledStatesFlow =
                            flowOf(
                                mapOf(
                                    target.readoutItemKey to true,
                                    AceWindowsReadoutItemKey.VehicleApproach.StartReadout to false,
                                    AceWindowsReadoutItemKey.Flag.WhiteFlag to false,
                                ),
                            ),
                        isCustomSpeakEvent = ::isAceWindowsCustomSpeakEvent,
                        customSpeak = { event, volume ->
                            assertEquals(target, event)
                            assertEquals(42, volume)
                            calls += "text"
                        },
                        scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
                    )
                runCurrent()
                AceWindowsNarratorEngine(engine).speak(target)
                runCurrent()
                assertEquals(listOf("start", "text"), calls)
                coVerify(exactly = index + 1) { soundPlayer.play(startSound, 42) }
            }
            confirmVerified(soundPlayer)
        }
}
