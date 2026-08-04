@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.acewindowsnarrator

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutStartSoundType
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsWavNarratorEngineTest {
    @Test
    fun `soundPlayer isPlaying が true でも実行中のジョブがなければ音声を再生する`() =
        runTest {
            val player = FakeSoundPlayer(isPlaying = true)
            val engine = createEngine(player)
            runCurrent()

            engine.speak(SpeechEvent.AceWindowsRemainingFuelWarning)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(REMAINING_FUEL_SOUND, player.playedSounds[1])
        }

    @Test
    fun `stop直後のspeakは前の再生の停止処理が完了するまで次の音声を再生しない`() =
        runTest {
            val cancellationSignal = CompletableDeferred<Unit>()
            val player =
                FakeSoundPlayer(blockingSound = REMAINING_FUEL_SOUND, cancellationSignal = cancellationSignal)
            val engine =
                createEngine(
                    player = player,
                    resourceLoader = { path -> if (path == RED_FLAG_PATH) RED_FLAG_SOUND else REMAINING_FUEL_SOUND },
                )
            runCurrent()

            engine.speak(SpeechEvent.AceWindowsRemainingFuelWarning)
            runCurrent()

            engine.stop()
            engine.speak(SpeechEvent.AceWindowsRedFlag)
            runCurrent()

            // 前の再生の停止処理（cancellationSignal の完了）を待っている間は次の音声を再生しない
            assertEquals(2, player.playedSounds.size)

            cancellationSignal.complete(Unit)
            advanceUntilIdle()

            assertEquals(4, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(REMAINING_FUEL_SOUND, player.playedSounds[1])
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[2])
            assertContentEquals(RED_FLAG_SOUND, player.playedSounds[3])
        }

    @Test
    fun `イベント音声が未ロードなら音声を再生しない`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player = player, resourceLoader = { error("load failed") })
            runCurrent()

            engine.speak(SpeechEvent.AceWindowsRemainingFuelWarning)
            runCurrent()

            assertEquals(emptyList(), player.playedSounds)
        }

    @Test
    fun `開始音とイベント音声を順番に再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.speak(SpeechEvent.AceWindowsRemainingFuelWarning)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(REMAINING_FUEL_SOUND, player.playedSounds[1])
        }

    @Test
    fun `電子ノイズを選択したとき電子ノイズ音声を再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine =
                createEngine(player = player, startSoundTypeFlow = flowOf(ReadoutStartSoundType.ELECTRONIC_NOISE))
            runCurrent()

            engine.speak(SpeechEvent.AceWindowsRemainingFuelWarning)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(ELECTRONIC_NOISE_SOUND, player.playedSounds[0])
            assertContentEquals(REMAINING_FUEL_SOUND, player.playedSounds[1])
        }

    @Test
    fun `開始音が未ロードでもイベント音声を再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player = player, startSoundResourceLoader = { error("load failed") })
            runCurrent()

            engine.speak(SpeechEvent.AceWindowsRemainingFuelWarning)
            runCurrent()

            assertEquals(1, player.playedSounds.size)
            assertContentEquals(REMAINING_FUEL_SOUND, player.playedSounds.single())
        }

    @Test
    fun `開始音タイプ変化後のspeakは新しい開始音で再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val startSoundTypeFlow = MutableStateFlow(ReadoutStartSoundType.FORMULA_RADIO)
            val engine = createEngine(player, startSoundTypeFlow = startSoundTypeFlow)
            runCurrent()

            engine.speak(SpeechEvent.AceWindowsRemainingFuelWarning)
            runCurrent()

            startSoundTypeFlow.update { ReadoutStartSoundType.ELECTRONIC_NOISE }
            runCurrent()

            engine.speak(SpeechEvent.AceWindowsRemainingFuelWarning)
            runCurrent()

            assertEquals(4, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(REMAINING_FUEL_SOUND, player.playedSounds[1])
            assertContentEquals(ELECTRONIC_NOISE_SOUND, player.playedSounds[2])
            assertContentEquals(REMAINING_FUEL_SOUND, player.playedSounds[3])
        }

    @Test
    fun `queue=trueで呼ぶと前の音声の後に続けて再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.speak(SpeechEvent.AceWindowsRemainingFuelWarning)
            engine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, queue = true)
            advanceUntilIdle()

            assertEquals(4, player.playedSounds.size)
        }

    @Test
    fun `stopを呼ぶと再生中のジョブがキャンセルされる`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.speak(SpeechEvent.AceWindowsRemainingFuelWarning)
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
            engine.speak(SpeechEvent.AceWindowsRemainingFuelWarning)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
        }

    @Test
    fun `volumeFlowの音量で再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player, volumeFlow = flowOf(50))
            runCurrent()

            engine.speak(SpeechEvent.AceWindowsRemainingFuelWarning)
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
            val engine = createEngine(player = player, startSoundResourceLoader = { error("load failed") })
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
        volumeFlow: Flow<Int> = flowOf(100),
        startSoundTypeFlow: Flow<ReadoutStartSoundType> = flowOf(ReadoutStartSoundType.FORMULA_RADIO),
        resourceLoader: suspend (String) -> ByteArray = { REMAINING_FUEL_SOUND },
        startSoundResourceLoader: suspend (String) -> ByteArray = { path ->
            when (path) {
                FORMULA_RADIO_PATH -> FORMULA_RADIO_SOUND
                ELECTRONIC_NOISE_PATH -> ELECTRONIC_NOISE_SOUND
                else -> FORMULA_RADIO_SOUND
            }
        },
    ): AceWindowsWavNarratorEngine =
        AceWindowsWavNarratorEngine(
            soundPlayer = player,
            volumeFlow = volumeFlow,
            startSoundTypeFlow = startSoundTypeFlow,
            resourceLoader = resourceLoader,
            startSoundResourceLoader = startSoundResourceLoader,
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )

    private companion object {
        const val FORMULA_RADIO_PATH = "files/formula_radio.wav"
        const val ELECTRONIC_NOISE_PATH = "files/electronic_noise.wav"
        const val RED_FLAG_PATH = "files/red_flag.wav"
        val REMAINING_FUEL_SOUND = byteArrayOf(1)
        val FORMULA_RADIO_SOUND = byteArrayOf(2)
        val ELECTRONIC_NOISE_SOUND = byteArrayOf(3)
        val RED_FLAG_SOUND = byteArrayOf(4)
    }
}

private class FakeSoundPlayer(
    override val isPlaying: Boolean = false,
    private val blockingSound: ByteArray? = null,
    private val cancellationSignal: CompletableDeferred<Unit>? = null,
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
            try {
                awaitCancellation()
            } finally {
                withContext(NonCancellable) {
                    cancellationSignal?.await()
                }
            }
        }
    }
}
