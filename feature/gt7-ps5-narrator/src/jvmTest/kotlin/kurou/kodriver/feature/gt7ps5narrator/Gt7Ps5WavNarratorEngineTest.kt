@file:Suppress("FunctionNaming", "TooManyFunctions")

package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
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
    fun `soundPlayer isPlaying が true でも実行中のジョブがなければ音声を再生する`() =
        runTest {
            val player = FakeSoundPlayer(isPlaying = true)
            val engine = createEngine(player)
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[1])
        }

    @Test
    fun `優先度の高い音声は前の再生の停止処理が完了してから再生される`() =
        runTest {
            val player = FakeSoundPlayer(blockingSound = MY_BEST_LAP_FORMAL_SOUND)
            val engine = createEngine(player)
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal)
            runCurrent()

            engine.stop()
            engine.speak(SpeechEvent.Gt7Ps5RemainingFuelWarning)
            advanceUntilIdle()

            assertEquals(4, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[1])
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[2])
            assertContentEquals(EVENT_SOUND, player.playedSounds[3])
        }

    @Test
    fun `イベント音声が未ロードなら音声を再生しない`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine =
                createEngine(
                    player = player,
                    resourceLoader = { error("load failed") },
                )
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal)
            runCurrent()

            assertEquals(emptyList(), player.playedSounds)
        }

    @Test
    fun `開始音とイベント音声を順番に再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[1])
        }

    @Test
    fun `MyBestLapCasual は casual 音声を再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine =
                createEngine(
                    player = player,
                    resourceLoader = { path ->
                        if (path == MY_BEST_LAP_CASUAL_PATH) MY_BEST_LAP_CASUAL_SOUND else EVENT_SOUND
                    },
                )
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5MyBestLapCasual)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(MY_BEST_LAP_CASUAL_SOUND, player.playedSounds[1])
        }

    @Test
    fun `電子ノイズを選択したとき電子ノイズ音声を再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine =
                createEngine(
                    player = player,
                    startSoundTypeFlow = flowOf(ReadoutStartSoundType.ELECTRONIC_NOISE),
                )
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(ELECTRONIC_NOISE_SOUND, player.playedSounds[0])
            assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[1])
        }

    @Test
    fun `開始音が未ロードでもイベント音声を再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine =
                createEngine(
                    player = player,
                    startSoundResourceLoader = { error("load failed") },
                )
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal)
            runCurrent()

            assertEquals(1, player.playedSounds.size)
            assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds.single())
        }

    @Test
    fun `開始音タイプ変化後のspeakは新しい開始音で再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val startSoundTypeFlow = MutableStateFlow(ReadoutStartSoundType.FORMULA_RADIO)
            val engine = createEngine(player, startSoundTypeFlow = startSoundTypeFlow)
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal)
            runCurrent()

            startSoundTypeFlow.update { ReadoutStartSoundType.ELECTRONIC_NOISE }
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal)
            runCurrent()

            assertEquals(4, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[1])
            assertContentEquals(ELECTRONIC_NOISE_SOUND, player.playedSounds[2])
            assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[3])
        }

    @Test
    fun `RemainingFuelLapsWarningを再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine =
                createEngine(
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
    fun `Gt7Ps5RemainingFuelWarningを再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine =
                createEngine(
                    player = player,
                    resourceLoader = { path ->
                        when (path) {
                            REMAINING_FUEL_CAUTION_PATH -> REMAINING_FUEL_CAUTION_SOUND
                            else -> EVENT_SOUND
                        }
                    },
                )
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5RemainingFuelWarning)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(REMAINING_FUEL_CAUTION_SOUND, player.playedSounds[1])
        }

    @Test
    fun `queue=trueで呼ぶと前の音声の後に続けて再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine =
                createEngine(
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
    fun `stopを呼ぶと再生中のジョブがキャンセルされる`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal)
            engine.stop()
            advanceUntilIdle()

            assertEquals(emptyList(), player.playedSounds)
        }

    @Test
    fun `stop後にspeakすると正常に再生できる`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.stop()
            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[1])
        }

    @Test
    fun `stopはキュー待機中のジョブも含めて全てキャンセルする`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal)
            engine.speak(SpeechEvent.RemainingFuelLapsWarning(3), queue = true)
            engine.stop()
            advanceUntilIdle()

            assertEquals(emptyList(), player.playedSounds)
        }

    @Test
    fun `queue false の speak は待機中のキューを破棄して新しい音声を再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5MyBestLapCasual)
            engine.speak(SpeechEvent.RemainingFuelLapsWarning(3), queue = true)
            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal)
            advanceUntilIdle()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[1])
        }

    @Test
    fun `stop後のqueue speakは正常に再生できる`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.stop()
            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, queue = true)
            advanceUntilIdle()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(MY_BEST_LAP_FORMAL_SOUND, player.playedSounds[1])
        }

    @Test
    fun `volumeFlowの音量で再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player, volumeFlow = flowOf(50))
            runCurrent()

            engine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal)
            runCurrent()

            assertEquals(listOf(50, 50), player.playedVolumes)
        }

    @Test
    fun `previewStartSoundは開始音のみを再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO)
            runCurrent()

            assertEquals(1, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds.single())
        }

    @Test
    fun `previewStartSoundは未ロードの開始音タイプなら何も再生しない`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine =
                createEngine(
                    player = player,
                    startSoundResourceLoader = { error("load failed") },
                )
            runCurrent()

            engine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO)
            runCurrent()

            assertEquals(emptyList(), player.playedSounds)
        }

    @Test
    fun `previewStartSoundはsoundPlayer isPlayingが trueでも実行中のジョブがなければ再生する`() =
        runTest {
            val player = FakeSoundPlayer(isPlaying = true)
            val engine = createEngine(player)
            runCurrent()

            engine.previewStartSound(ReadoutStartSoundType.FORMULA_RADIO)
            runCurrent()

            assertEquals(1, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds.single())
        }

    private fun TestScope.createEngine(
        player: FakeSoundPlayer,
        volumeFlow: kotlinx.coroutines.flow.Flow<Int> = flowOf(100),
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
    ): Gt7Ps5WavNarratorEngine =
        Gt7Ps5WavNarratorEngine(
            soundPlayer = player,
            volumeFlow = volumeFlow,
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
        const val REMAINING_FUEL_CAUTION_PATH = "files/remaining_fuel_caution.wav"
        val MY_BEST_LAP_FORMAL_SOUND = byteArrayOf(1)
        val MY_BEST_LAP_CASUAL_SOUND = byteArrayOf(2)
        val EVENT_SOUND = byteArrayOf(3)
        val FORMULA_RADIO_SOUND = byteArrayOf(4)
        val ELECTRONIC_NOISE_SOUND = byteArrayOf(5)
        val REMAINING_FUEL_LAPS_0_SOUND = byteArrayOf(6)
        val REMAINING_FUEL_LAPS_3_SOUND = byteArrayOf(7)
        val REMAINING_FUEL_CAUTION_SOUND = byteArrayOf(8)
    }
}

private class FakeSoundPlayer(
    override val isPlaying: Boolean = false,
    private val blockingSound: ByteArray? = null,
) : SoundPlayer {
    val playedSounds = mutableListOf<ByteArray>()
    val playedVolumes = mutableListOf<Int>()

    override suspend fun play(
        bytes: ByteArray,
        volume: Int,
    ) {
        playedSounds += bytes
        playedVolumes += volume
        if (blockingSound != null && bytes.contentEquals(blockingSound)) {
            awaitCancellation()
        }
    }
}
