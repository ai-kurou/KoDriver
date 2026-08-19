package kurou.kodriver.core.narrator

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
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private typealias TestEngine = WavNarratorEngine<String, String, String>

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
class WavNarratorEngineTest {
    @Test
    fun `soundPlayer isPlaying が true でも実行中のジョブがなければ音声を再生する`() =
        runTest {
            val player = FakeSoundPlayer(isPlaying = true)
            val engine = createEngine(player)
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
        }

    @Test
    fun `stop直後のspeakは前の再生の停止処理が完了するまで次の音声を再生しない`() =
        runTest {
            val cancellationSignal = CompletableDeferred<Unit>()
            val player = FakeSoundPlayer(blockingSound = CAR_LEFT_SOUND, cancellationSignal = cancellationSignal)
            val engine = createEngine(player)
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            engine.stop()
            engine.speak(RED_FLAG)
            runCurrent()

            // 前の再生の停止処理（cancellationSignal の完了）を待っている間は次の音声を再生しない
            assertEquals(2, player.playedSounds.size)

            cancellationSignal.complete(Unit)
            advanceUntilIdle()

            assertEquals(4, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[2])
            assertContentEquals(RED_FLAG_SOUND, player.playedSounds[3])
        }

    @Test
    fun `speakがspeakに連続で割り込む場合も前の再生の停止処理が完了するまで次の音声を再生しない`() =
        runTest {
            val cancellationSignal = CompletableDeferred<Unit>()
            val player = FakeSoundPlayer(blockingSound = CAR_LEFT_SOUND, cancellationSignal = cancellationSignal)
            val engine = createEngine(player)
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            // stop() を挟まず speak() が speak() に直接割り込むケース
            engine.speak(RED_FLAG)
            runCurrent()

            // 前の再生（CAR_LEFT）の停止処理（cancellationSignal の完了）を待っている間は次の音声を再生しない
            assertEquals(2, player.playedSounds.size)

            cancellationSignal.complete(Unit)
            advanceUntilIdle()

            assertEquals(4, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[2])
            assertContentEquals(RED_FLAG_SOUND, player.playedSounds[3])
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

            engine.speak(CAR_LEFT)
            runCurrent()

            assertEquals(emptyList(), player.playedSounds)
        }

    @Test
    fun `eventToFileに存在しないイベントは音声を再生しない`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.speak(UNKNOWN_EVENT)
            runCurrent()

            assertEquals(emptyList(), player.playedSounds)
        }

    @Test
    fun `開始音とイベント音声を順番に再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
        }

    @Test
    fun `電子ノイズを選択したとき電子ノイズ音声を再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine =
                createEngine(
                    player = player,
                    startSoundTypeFlow = flowOf(ELECTRONIC_NOISE),
                )
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(ELECTRONIC_NOISE_SOUND, player.playedSounds[0])
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
        }

    @Test
    fun `開始音が無効な項目はイベント音声のみを再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine =
                createEngine(
                    player = player,
                    startSoundEnabledStatesFlow = flowOf(mapOf(CAR_LEFT_KEY to false)),
                )
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            assertEquals(1, player.playedSounds.size)
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds.single())
        }

    @Test
    fun `開始音の有効無効状態変化後のspeakは新しい状態を反映する`() =
        runTest {
            val player = FakeSoundPlayer()
            val startSoundEnabledStatesFlow = MutableStateFlow(mapOf(CAR_LEFT_KEY to true))
            val engine = createEngine(player, startSoundEnabledStatesFlow = startSoundEnabledStatesFlow)
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            startSoundEnabledStatesFlow.update { mapOf(CAR_LEFT_KEY to false) }
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            assertEquals(3, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[2])
        }

    @Test
    fun `queue true の speak は前の音声が終わってから再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.speak(CAR_LEFT)
            engine.speak(RED_FLAG, queue = true)
            advanceUntilIdle()

            assertEquals(4, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[2])
            assertContentEquals(RED_FLAG_SOUND, player.playedSounds[3])
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

            engine.speak(CAR_LEFT)
            runCurrent()

            assertEquals(1, player.playedSounds.size)
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds.single())
        }

    @Test
    fun `volumeFlowで指定した音量で開始音とイベント音声を再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player, volumeFlow = flowOf(50))
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            assertEquals(listOf(50, 50), player.playedVolumes)
        }

    @Test
    fun `音量変化後のspeakは新しい音量で再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val volumeFlow = MutableStateFlow(80)
            val engine = createEngine(player, volumeFlow = volumeFlow)
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            volumeFlow.update { 30 }
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            assertEquals(listOf(80, 80, 30, 30), player.playedVolumes)
        }

    @Test
    fun `開始音タイプ変化後のspeakは新しい開始音で再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val startSoundTypeFlow = MutableStateFlow(FORMULA_RADIO)
            val engine = createEngine(player, startSoundTypeFlow = startSoundTypeFlow)
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            startSoundTypeFlow.update { ELECTRONIC_NOISE }
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            assertEquals(4, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
            assertContentEquals(ELECTRONIC_NOISE_SOUND, player.playedSounds[2])
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[3])
        }

    @Test
    fun `stopを呼ぶと再生中のジョブがキャンセルされる`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.speak(CAR_LEFT)
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
            engine.speak(CAR_LEFT)
            runCurrent()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
        }

    @Test
    fun `stopはキュー待機中のジョブも含めて全てキャンセルする`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.speak(CAR_LEFT)
            engine.speak(RED_FLAG, queue = true)
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

            engine.speak(LEFT_APPROACH)
            engine.speak(RED_FLAG, queue = true)
            engine.speak(CAR_LEFT)
            advanceUntilIdle()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
        }

    @Test
    fun `stop後のqueue speakは正常に再生できる`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.stop()
            engine.speak(CAR_LEFT, queue = true)
            advanceUntilIdle()

            assertEquals(2, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds[0])
            assertContentEquals(CAR_LEFT_SOUND, player.playedSounds[1])
        }

    @Test
    fun `previewStartSoundは開始音のみを再生する`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            engine.previewStartSound(FORMULA_RADIO)
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

            engine.previewStartSound(FORMULA_RADIO)
            runCurrent()

            assertEquals(emptyList(), player.playedSounds)
        }

    @Test
    fun `previewStartSoundはsoundPlayer isPlayingが trueでも実行中のジョブがなければ再生する`() =
        runTest {
            val player = FakeSoundPlayer(isPlaying = true)
            val engine = createEngine(player)
            runCurrent()

            engine.previewStartSound(FORMULA_RADIO)
            runCurrent()

            assertEquals(1, player.playedSounds.size)
            assertContentEquals(FORMULA_RADIO_SOUND, player.playedSounds.single())
        }

    @Test
    fun `currentKeyは再生中のイベントに対応するキーを返す`() =
        runTest {
            val cancellationSignal = CompletableDeferred<Unit>()
            val player = FakeSoundPlayer(blockingSound = CAR_LEFT_SOUND, cancellationSignal = cancellationSignal)
            val engine = createEngine(player)
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()

            assertEquals(CAR_LEFT_KEY, engine.currentKey)

            cancellationSignal.complete(Unit)
            advanceUntilIdle()
        }

    @Test
    fun `再生していない場合currentKeyはnullを返す`() =
        runTest {
            val player = FakeSoundPlayer()
            val engine = createEngine(player)
            runCurrent()

            assertEquals(null, engine.currentKey)
        }

    @Test
    fun `stopで中断した後currentKeyはnullを返す`() =
        runTest {
            val cancellationSignal = CompletableDeferred<Unit>()
            val player = FakeSoundPlayer(blockingSound = CAR_LEFT_SOUND, cancellationSignal = cancellationSignal)
            val engine = createEngine(player)
            runCurrent()

            engine.speak(CAR_LEFT)
            runCurrent()
            assertEquals(CAR_LEFT_KEY, engine.currentKey)

            engine.stop()
            runCurrent()

            assertEquals(null, engine.currentKey)

            cancellationSignal.complete(Unit)
            advanceUntilIdle()
        }

    private fun TestScope.createEngine(
        player: FakeSoundPlayer,
        volumeFlow: Flow<Int> = flowOf(100),
        startSoundTypeFlow: Flow<String> = flowOf(FORMULA_RADIO),
        startSoundEnabledStatesFlow: Flow<Map<String, Boolean>> = flowOf(emptyMap()),
        resourceLoader: suspend (String) -> ByteArray = { path ->
            when (path) {
                CAR_LEFT_PATH -> CAR_LEFT_SOUND
                LEFT_APPROACH_PATH -> LEFT_APPROACH_SOUND
                RED_FLAG_PATH -> RED_FLAG_SOUND
                else -> error("unexpected path: $path")
            }
        },
        startSoundResourceLoader: suspend (String) -> ByteArray = { path ->
            when (path) {
                FORMULA_RADIO_PATH -> FORMULA_RADIO_SOUND
                ELECTRONIC_NOISE_PATH -> ELECTRONIC_NOISE_SOUND
                else -> error("unexpected path: $path")
            }
        },
    ): TestEngine =
        WavNarratorEngine(
            soundPlayer = player,
            resources =
                WavResources(
                    eventToFile =
                        mapOf(
                            CAR_LEFT to CAR_LEFT_PATH,
                            LEFT_APPROACH to LEFT_APPROACH_PATH,
                            RED_FLAG to RED_FLAG_PATH,
                        ),
                    startSoundTypeToFile =
                        mapOf(
                            FORMULA_RADIO to FORMULA_RADIO_PATH,
                            ELECTRONIC_NOISE to ELECTRONIC_NOISE_PATH,
                        ),
                    resourceLoader = resourceLoader,
                    startSoundResourceLoader = startSoundResourceLoader,
                ),
            eventToKey = { event -> "${event}_key" },
            defaultStartSoundType = FORMULA_RADIO,
            volumeFlow = volumeFlow,
            startSoundTypeFlow = startSoundTypeFlow,
            startSoundEnabledStatesFlow = startSoundEnabledStatesFlow,
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )

    private companion object {
        const val CAR_LEFT = "car_left"
        const val LEFT_APPROACH = "left_approach"
        const val RED_FLAG = "red_flag"
        const val UNKNOWN_EVENT = "unknown_event"
        const val FORMULA_RADIO = "formula_radio"
        const val ELECTRONIC_NOISE = "electronic_noise"
        const val CAR_LEFT_KEY = "car_left_key"
        const val CAR_LEFT_PATH = "files/car_left.wav"
        const val LEFT_APPROACH_PATH = "files/left_approach.wav"
        const val RED_FLAG_PATH = "files/red_flag.wav"
        const val FORMULA_RADIO_PATH = "files/formula_radio.wav"
        const val ELECTRONIC_NOISE_PATH = "files/electronic_noise.wav"
        val CAR_LEFT_SOUND = byteArrayOf(1)
        val LEFT_APPROACH_SOUND = byteArrayOf(2)
        val RED_FLAG_SOUND = byteArrayOf(3)
        val FORMULA_RADIO_SOUND = byteArrayOf(4)
        val ELECTRONIC_NOISE_SOUND = byteArrayOf(5)
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
