package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
class LmuWindowsWavNarratorEngineTest {

    @Test
    fun `再生中は音声を再生しない`() = runTest {
        val player = FakeSoundPlayer(isPlaying = true)
        val engine = createEngine(player)
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
        runCurrent()

        assertEquals(emptyList(), player.playedSounds)
    }

    @Test
    fun `イベント音声が未ロードなら音声を再生しない`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(
            player = player,
            resourceLoader = { path ->
                if (path == FORMULA_RADIO_PATH) FORMULA_RADIO_SOUND else error("load failed")
            },
        )
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
        runCurrent()

        assertEquals(emptyList(), player.playedSounds)
    }

    @Test
    fun `開始音とイベント音声を順番に再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(player)
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
        runCurrent()

        assertEquals(2, player.playedSounds.size)
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
        assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
    }

    @Test
    fun `電子ノイズを選択したとき電子ノイズ音声を再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(
            player = player,
            startSoundTypeFlow = flowOf(ReadoutStartSoundType.ELECTRONIC_NOISE),
            resourceLoader = { path ->
                when (path) {
                    CAR_LEFT_PATH -> CAR_LEFT_SOUND
                    ELECTRONIC_NOISE_PATH -> ELECTRONIC_NOISE_SOUND
                    else -> EVENT_SOUND
                }
            },
        )
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
        runCurrent()

        assertEquals(2, player.playedSounds.size)
        assertContentEquals(ELECTRONIC_NOISE_SOUND, player.playedSounds[0])
        assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
    }

    @Test
    fun `LeftApproach は左接近音声を再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(
            player = player,
            resourceLoader = { path ->
                when (path) {
                    LEFT_APPROACH_PATH -> LEFT_APPROACH_SOUND
                    FORMULA_RADIO_PATH -> FORMULA_RADIO_SOUND
                    else -> EVENT_SOUND
                }
            },
        )
        runCurrent()

        engine.speak(SpeechEvent.LeftApproach)
        runCurrent()

        assertEquals(2, player.playedSounds.size)
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
        assertContentEquals(LEFT_APPROACH_SOUND, player.playedSounds[1])
    }

    @Test
    fun `queue true の speak は前の音声が終わってから再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(player)
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
        engine.speak(SpeechEvent.CarRight, queue = true)
        advanceUntilIdle()

        assertEquals(4, player.playedSounds.size)
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
        assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[2])
        assertContentEquals(EVENT_SOUND, player.playedSounds[3])
    }

    @Test
    fun `開始音が未ロードでもイベント音声を再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(
            player = player,
            resourceLoader = { path ->
                when (path) {
                    CAR_LEFT_PATH -> CAR_LEFT_SOUND
                    FORMULA_RADIO_PATH -> error("load failed")
                    else -> EVENT_SOUND
                }
            },
        )
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
        runCurrent()

        assertEquals(1, player.playedSounds.size)
        assertContentEquals(CAR_LEFT_SOUND, player.playedSounds.single())
    }

    @Test
    fun `volumeFlowで指定した音量で開始音とイベント音声を再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(player, volumeFlow = flowOf(50))
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
        runCurrent()

        assertEquals(listOf(50, 50), player.playedVolumes)
    }

    @Test
    fun `音量変化後のspeakは新しい音量で再生する`() = runTest {
        val player = FakeSoundPlayer()
        val volumeFlow = MutableStateFlow(80)
        val engine = createEngine(player, volumeFlow = volumeFlow)
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
        runCurrent()

        volumeFlow.update { 30 }
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
        runCurrent()

        assertEquals(listOf(80, 80, 30, 30), player.playedVolumes)
    }

    @Test
    fun `開始音タイプ変化後のspeakは新しい開始音で再生する`() = runTest {
        val player = FakeSoundPlayer()
        val startSoundTypeFlow = MutableStateFlow(ReadoutStartSoundType.FORMULA_RADIO)
        val engine = createEngine(player, startSoundTypeFlow = startSoundTypeFlow)
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
        runCurrent()

        startSoundTypeFlow.update { ReadoutStartSoundType.ELECTRONIC_NOISE }
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
        runCurrent()

        assertEquals(4, player.playedSounds.size)
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
        assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
        assertContentEquals(ELECTRONIC_NOISE_SOUND, player.playedSounds[2])
        assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[3])
    }

    @Test
    fun `stopを呼ぶと再生中のジョブがキャンセルされる`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(player)
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
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
        engine.speak(SpeechEvent.CarLeft)
        runCurrent()

        assertEquals(2, player.playedSounds.size)
        assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
        assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
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
            resourceLoader = { path ->
                when (path) {
                    CAR_LEFT_PATH -> CAR_LEFT_SOUND
                    FORMULA_RADIO_PATH -> error("load failed")
                    else -> EVENT_SOUND
                }
            },
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
        volumeFlow: Flow<Int> = flowOf(100),
        startSoundTypeFlow: Flow<ReadoutStartSoundType> = flowOf(ReadoutStartSoundType.FORMULA_RADIO),
        resourceLoader: suspend (String) -> ByteArray = { path ->
            when (path) {
                CAR_LEFT_PATH -> CAR_LEFT_SOUND
                FORMULA_RADIO_PATH -> FORMULA_RADIO_SOUND
                ELECTRONIC_NOISE_PATH -> ELECTRONIC_NOISE_SOUND
                else -> EVENT_SOUND
            }
        },
    ): LmuWindowsWavNarratorEngine = LmuWindowsWavNarratorEngine(
        soundPlayer = player,
        volumeFlow = volumeFlow,
        startSoundTypeFlow = startSoundTypeFlow,
        resourceLoader = resourceLoader,
        scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
    )

    private companion object {
        const val CAR_LEFT_PATH = "files/car_left.wav"
        const val LEFT_APPROACH_PATH = "files/left_approach.wav"
        const val FORMULA_RADIO_PATH = "files/formula_radio.wav"
        const val ELECTRONIC_NOISE_PATH = "files/electronic_noise.wav"
        val CAR_LEFT_SOUND = byteArrayOf(1)
        val EVENT_SOUND = byteArrayOf(2)
        val FORMULA_RADIO_SOUND = byteArrayOf(3)
        val ELECTRONIC_NOISE_SOUND = byteArrayOf(5)
        val LEFT_APPROACH_SOUND = byteArrayOf(4)
    }
}

private class FakeSoundPlayer(
    override val isPlaying: Boolean = false,
) : SoundPlayer {
    val playedSounds = mutableListOf<ByteArray>()
    val playedVolumes = mutableListOf<Int>()

    override suspend fun play(bytes: ByteArray, volume: Int) {
        playedSounds += bytes
        playedVolumes += volume
    }
}
