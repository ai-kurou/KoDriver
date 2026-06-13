package kurou.kodriver.feature.lmunarrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechEvent
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWavNarratorEngineTest {

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
                if (path == NOISE_PATH) NOISE_SOUND else error("load failed")
            },
        )
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
        runCurrent()

        assertEquals(emptyList(), player.playedSounds)
    }

    @Test
    fun `ノイズとイベント音声を順番に再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(player)
        runCurrent()

        engine.speak(SpeechEvent.CarLeft)
        runCurrent()

        assertEquals(2, player.playedSounds.size)
        assertContentEquals(NOISE_SOUND, player.playedSounds[0])
        assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
    }

    @Test
    fun `ノイズが未ロードでもイベント音声を再生する`() = runTest {
        val player = FakeSoundPlayer()
        val engine = createEngine(
            player = player,
            resourceLoader = { path ->
                when (path) {
                    CAR_LEFT_PATH -> CAR_LEFT_SOUND
                    NOISE_PATH -> error("load failed")
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

    private fun TestScope.createEngine(
        player: FakeSoundPlayer,
        resourceLoader: suspend (String) -> ByteArray = { path ->
            when (path) {
                CAR_LEFT_PATH -> CAR_LEFT_SOUND
                NOISE_PATH -> NOISE_SOUND
                else -> EVENT_SOUND
            }
        },
    ): LmuWavNarratorEngine = LmuWavNarratorEngine(
        soundPlayer = player,
        resourceLoader = resourceLoader,
        scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
    )

    private companion object {
        const val CAR_LEFT_PATH = "files/car_left.wav"
        const val NOISE_PATH = "files/noise.wav"
        val CAR_LEFT_SOUND = byteArrayOf(1)
        val EVENT_SOUND = byteArrayOf(2)
        val NOISE_SOUND = byteArrayOf(3)
    }
}

private class FakeSoundPlayer(
    override val isPlaying: Boolean = false,
) : SoundPlayer {
    val playedSounds = mutableListOf<ByteArray>()

    override suspend fun play(bytes: ByteArray) {
        playedSounds += bytes
    }
}
