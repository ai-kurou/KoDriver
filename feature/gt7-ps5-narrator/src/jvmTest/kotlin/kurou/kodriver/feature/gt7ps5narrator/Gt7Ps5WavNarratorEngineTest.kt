@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutStartSoundType
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5WavNarratorEngineTest {

    @Test
    fun `再生中は音声を再生しない`() = runTest {
        val player = FakeSoundPlayer(isPlaying = true)
        val engine = createEngine(player)
        runCurrent()

        engine.speak(SpeechEvent.MyBestLapFormal)
        runCurrent()

        assertEquals(emptyList(), player.playedSounds)
    }

    @Test
    fun `イベント音声が未ロードなら音声を再生しない`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(
            player = player,
            resourceLoader = { error("load failed") },
        )
        runCurrent()

        engine.speak(SpeechEvent.MyBestLapFormal)
        runCurrent()

        assertEquals(emptyList(), player.playedSounds)
    }

    @Test
    fun `開始音とイベント音声を順番に再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(player)
        runCurrent()

        engine.speak(SpeechEvent.MyBestLapFormal)
        runCurrent()

        assertEquals(2, player.playedSounds.size)
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
        assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[1])
    }

    @Test
    fun `MyBestLapCasual は casual 音声を再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(
            player = player,
            resourceLoader = { path ->
                if (path == MY_BEST_LAP_CASUAL_PATH) MY_BEST_LAP_CASUAL_SOUND else EVENT_SOUND
            },
        )
        runCurrent()

        engine.speak(SpeechEvent.MyBestLapCasual)
        runCurrent()

        assertEquals(2, player.playedSounds.size)
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
        assertContentEquals(MY_BEST_LAP_CASUAL_SOUND, player.playedSounds[1])
    }

    @Test
    fun `電子ノイズを選択したとき電子ノイズ音声を再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(
            player = player,
            startSoundTypeFlow = flowOf(ReadoutStartSoundType.ELECTRONIC_NOISE),
        )
        runCurrent()

        engine.speak(SpeechEvent.MyBestLapFormal)
        runCurrent()

        assertEquals(2, player.playedSounds.size)
        assertContentEquals(ELECTRONIC_NOISE_SOUND, player.playedSounds[0])
        assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[1])
    }

    @Test
    fun `開始音が未ロードでもイベント音声を再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(
            player = player,
            startSoundResourceLoader = { error("load failed") },
        )
        runCurrent()

        engine.speak(SpeechEvent.MyBestLapFormal)
        runCurrent()

        assertEquals(1, player.playedSounds.size)
        assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds.single())
    }

    @Test
    fun `開始音タイプ変化後のspeakは新しい開始音で再生する`() = runTest {
        val player = FakeSoundPlayer()
        val startSoundTypeFlow = MutableStateFlow(ReadoutStartSoundType.FORMULA_RADIO)
        val engine = createEngine(player, startSoundTypeFlow = startSoundTypeFlow)
        runCurrent()

        engine.speak(SpeechEvent.MyBestLapFormal)
        runCurrent()

        startSoundTypeFlow.update { ReadoutStartSoundType.ELECTRONIC_NOISE }
        runCurrent()

        engine.speak(SpeechEvent.MyBestLapFormal)
        runCurrent()

        assertEquals(4, player.playedSounds.size)
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
        assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[1])
        assertContentEquals(ELECTRONIC_NOISE_SOUND, player.playedSounds[2])
        assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[3])
    }

    @Test
    fun `RemainingFuelLapsWarningを再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(
            player = player,
            resourceLoader = { path ->
                when (path) {
                    REMAINING_FUEL_LAPS_3_PATH -> REMAINING_FUEL_LAPS_3_SOUND
                    else -> EVENT_SOUND
                }
            },
        )
        runCurrent()

        engine.speak(SpeechEvent.RemainingFuelLapsWarning(3))
        runCurrent()

        assertEquals(2, player.playedSounds.size)
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
        assertContentEquals(REMAINING_FUEL_LAPS_3_SOUND, player.playedSounds[1])
    }

    @Test
    fun `queue=trueで呼ぶと前の音声の後に続けて再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(
            player = player,
            resourceLoader = { path ->
                when (path) {
                    REMAINING_FUEL_LAPS_3_PATH -> REMAINING_FUEL_LAPS_3_SOUND
                    REMAINING_FUEL_LAPS_0_PATH -> REMAINING_FUEL_LAPS_0_SOUND
                    else -> EVENT_SOUND
                }
            },
        )
        runCurrent()

        engine.speak(SpeechEvent.RemainingFuelLapsWarning(3))
        engine.speak(SpeechEvent.RemainingFuelLapsWarning(0), queue = true)
        advanceUntilIdle()

        assertEquals(4, player.playedSounds.size)
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
        assertContentEquals(REMAINING_FUEL_LAPS_3_SOUND, player.playedSounds[1])
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[2])
        assertContentEquals(REMAINING_FUEL_LAPS_0_SOUND, player.playedSounds[3])
    }

    @Test
    fun `stopを呼ぶと再生中のジョブがキャンセルされる`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(player)
        runCurrent()

        engine.speak(SpeechEvent.MyBestLapFormal)
        engine.stop()
        advanceUntilIdle()

        assertEquals(emptyList(), player.playedSounds)
    }

    @Test
    fun `stop後にspeakすると正常に再生できる`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(player)
        runCurrent()

        engine.stop()
        engine.speak(SpeechEvent.MyBestLapFormal)
        runCurrent()

        assertEquals(2, player.playedSounds.size)
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
        assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[1])
    }

    @Test
    fun `previewStartSoundは開始音のみを再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(player)
        runCurrent()

        engine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO)
        runCurrent()

        assertEquals(1, player.playedSounds.size)
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds.single())
    }

    @Test
    fun `previewStartSoundは未ロードの開始音タイプなら何も再生しない`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(
            player = player,
            startSoundResourceLoader = { error("load failed") },
        )
        runCurrent()

        engine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO)
        runCurrent()

        assertEquals(emptyList(), player.playedSounds)
    }

    @Test
    fun `previewStartSoundは再生中なら何も再生しない`() = runTest {
        val player = FakeSoundPlayer(isPlaying = true)
        val engine = createEngine(player)
        runCurrent()

        engine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO)
        runCurrent()

        assertEquals(emptyList(), player.playedSounds)
    }

    private fun TestScope.createEngine(
        player: FakeSoundPlayer,
        startSoundTypeFlow: kotlinx.coroutines.flow.Flow<ReadoutStartSoundType> =
            flowOf(ReadoutStartSoundType.FORMULA_RADIO),
        resourceLoader: suspend (String) -> ByteArray = { path ->
            when (path) {
                MY_BEST_LAP_FORMAL_PATH -> MY_BEST_LAP_FORMAL_SOUND
                else -> EVENT_SOUND
            }
        },
        startSoundResourceLoader: suspend (String) -> ByteArray = { path ->
            when (path) {
                FORMULA_RADIO_PATH -> FORMULA_RADIO_SOUND
                ELECTRONIC_NOISE_PATH -> ELECTRONIC_NOISE_SOUND
                else -> FORMULA_RADIO_SOUND
            }
        },
    ): Gt7Ps5WavNarratorEngine = Gt7Ps5WavNarratorEngine(
        soundPlayer = player,
        startSoundTypeFlow = startSoundTypeFlow,
        resourceLoader = resourceLoader,
        startSoundResourceLoader = startSoundResourceLoader,
        scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
    )

    private companion object {
        const val MY_BEST_LAP_FORMAL_PATH = "files/my_best_lap_formal.wav"
        const val MY_BEST_LAP_CASUAL_PATH = "files/my_best_lap_casual.wav"
        const val FORMULA_RADIO_PATH = "files/formula_radio.wav"
        const val ELECTRONIC_NOISE_PATH = "files/electronic_noise.wav"
        const val REMAINING_FUEL_LAPS_0_PATH = "files/remaining_fuel_laps_0.wav"
        const val REMAINING_FUEL_LAPS_3_PATH = "files/remaining_fuel_laps_3.wav"
        val MY_BEST_LAP_FORMAL_SOUND = byteArrayOf(1)
        val MY_BEST_LAP_CASUAL_SOUND = byteArrayOf(2)
        val EVENT_SOUND = byteArrayOf(3)
        val FORMULA_RADIO_SOUND = byteArrayOf(4)
        val ELECTRONIC_NOISE_SOUND = byteArrayOf(5)
        val REMAINING_FUEL_LAPS_0_SOUND = byteArrayOf(6)
        val REMAINING_FUEL_LAPS_3_SOUND = byteArrayOf(7)
    }
}

private class FakeSoundPlayer(
    override val isPlaying: Boolean = false,
) : SoundPlayer {
    val playedSounds = mutableListOf<ByteArray>()

    override suspend fun play(bytes: ByteArray, volume: Int) {
        playedSounds += bytes
    }
}
