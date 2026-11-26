package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionEvent
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionMode
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecognitionPipelineTest {
    private val modes = mutableListOf<SpeechRecognitionMode>()
    private val results = ArrayDeque<SpeechTranscript?>()
    private val accepted = mutableListOf<ByteArray>()
    private val transcriber =
        object : SpeechTranscriber {
            override fun accept(pcm: ByteArray): SpeechTranscript? {
                accepted += pcm
                return results.removeFirst()
            }

            override fun setMode(mode: SpeechRecognitionMode) {
                modes += mode
            }

            override fun close() = Unit
        }
    private val mode = MutableStateFlow(SpeechRecognitionMode.WakeWord)
    private val threshold = MutableStateFlow(0.3f)
    private val cutInCommand = MutableStateFlow(false)
    private val pipeline = RecognitionPipeline(transcriber, mode, threshold, cutInCommand)

    private suspend fun collectEvents(vararg steps: () -> Unit): List<SpeechRecognitionEvent> =
        flow<SpeechRecognitionEvent> {
            steps.forEach { step ->
                step()
                with(pipeline) { process(ByteArray(4)) }
            }
        }.toList()

    @Test
    fun `音量を流し、認識結果がなければそれだけを流す`() =
        runTest {
            results += null

            val events = collectEvents({})

            assertEquals(1, events.size)
            assertTrue(events.single() is SpeechRecognitionEvent.Level)
        }

    @Test
    fun `同じ途中経過は続けて流さない`() =
        runTest {
            results += SpeechTranscript.Partial("a")
            results += SpeechTranscript.Partial("a")
            results += SpeechTranscript.Partial("ab")

            val partials = collectEvents({}, {}, {}).filterIsInstance<SpeechRecognitionEvent.Partial>()

            assertEquals(listOf("a", "ab"), partials.map { it.text })
        }

    @Test
    fun `確定したら候補を流し、同じ途中経過が再び来たら流す`() =
        runTest {
            results += SpeechTranscript.Partial("a")
            results += SpeechTranscript.Final(listOf("a", "b"))
            results += SpeechTranscript.Partial("a")

            val events = collectEvents({}, {}, {}).filterNot { it is SpeechRecognitionEvent.Level }

            assertEquals(
                listOf(
                    SpeechRecognitionEvent.Partial("a"),
                    SpeechRecognitionEvent.Recognized(listOf("a", "b")),
                    SpeechRecognitionEvent.Partial("a"),
                ),
                events,
            )
        }

    @Test
    fun `モードが変わったら途中経過を引き継がず、認識器へ反映する`() =
        runTest {
            results += SpeechTranscript.Partial("a")
            results += SpeechTranscript.Partial("a")

            val partials =
                collectEvents({}, { mode.value = SpeechRecognitionMode.Command })
                    .filterIsInstance<SpeechRecognitionEvent.Partial>()

            assertEquals(2, partials.size)
            assertEquals(listOf(SpeechRecognitionMode.Command), modes)
        }

    @Test
    fun `聞き取りを作り直しても、同じ反映状態を渡せば戻されたモードを認識器へ反映する`() =
        runTest {
            val modeSync = TranscriberModeSync(transcriber, mode)
            results += null
            results += null

            mode.value = SpeechRecognitionMode.Paused
            processOnce(RecognitionPipeline(transcriber, modeSync, threshold, cutInCommand))
            mode.value = SpeechRecognitionMode.WakeWord
            processOnce(RecognitionPipeline(transcriber, modeSync, threshold, cutInCommand))

            assertEquals(listOf(SpeechRecognitionMode.Paused, SpeechRecognitionMode.WakeWord), modes)
        }

    @Test
    fun `閾値未満の断片は全ゼロを認識器に渡す`() =
        runTest {
            results += null

            feed(quietPcm)

            assertContentEquals(ByteArray(quietPcm.size), accepted.single())
        }

    @Test
    fun `閾値以上の断片は元の音声を認識器に渡す`() =
        runTest {
            results += null

            feed(loudPcm)

            assertContentEquals(loudPcm, accepted.single())
        }

    @Test
    fun `閾値以上のあとはホールド回数ぶんの小さい断片を通し、超えると全ゼロを渡す`() =
        runTest {
            repeat(VOLUME_GATE_HOLD_CHUNKS + 2) { results += null }

            feed(loudPcm, *Array(VOLUME_GATE_HOLD_CHUNKS + 1) { quietPcm })

            assertContentEquals(loudPcm, accepted.first())
            repeat(VOLUME_GATE_HOLD_CHUNKS) { i -> assertContentEquals(quietPcm, accepted[i + 1]) }
            assertContentEquals(ByteArray(quietPcm.size), accepted.last())
        }

    @Test
    fun `ゲートで無音にしても音量は実音量のまま流す`() =
        runTest {
            results += null

            val events = feed(quietPcm)

            assertEquals(SpeechRecognitionEvent.Level(quietPcm.pcmLevel()), events.single())
            assertTrue(quietPcm.pcmLevel() < threshold.value)
        }

    @Test
    fun `閾値が0なら小さい断片も元の音声を認識器に渡す`() =
        runTest {
            results += null
            threshold.value = 0f

            feed(quietPcm)

            assertContentEquals(quietPcm, accepted.single())
        }

    @Test
    fun `閾値を上げると大きい断片でも閾値未満なら全ゼロを渡す`() =
        runTest {
            results += null
            threshold.value = loudPcm.pcmLevel() + 0.001f

            feed(loudPcm)

            assertContentEquals(ByteArray(loudPcm.size), accepted.single())
        }

    @Test
    fun `閾値を聞き取りの途中で変えると次の断片から反映する`() =
        runTest {
            repeat(2) { results += null }

            flow<SpeechRecognitionEvent> {
                with(pipeline) { process(quietPcm) }
                threshold.value = 0f
                with(pipeline) { process(quietPcm) }
            }.toList()

            assertContentEquals(ByteArray(quietPcm.size), accepted[0])
            assertContentEquals(quietPcm, accepted[1])
        }

    @Test
    fun `コマンド受付からウェイクワードに戻ったら、前のホールドを持ち越さず小さい断片は全ゼロを渡す`() =
        runTest {
            repeat(3) { results += null }

            flow<SpeechRecognitionEvent> {
                with(pipeline) { process(loudPcm) }
                mode.value = SpeechRecognitionMode.Command
                with(pipeline) { process(quietPcm) }
                mode.value = SpeechRecognitionMode.WakeWord
                with(pipeline) { process(quietPcm) }
            }.toList()

            assertContentEquals(quietPcm, accepted[1])
            assertContentEquals(ByteArray(quietPcm.size), accepted[2])
        }

    private suspend fun feed(vararg chunks: ByteArray): List<SpeechRecognitionEvent> =
        flow<SpeechRecognitionEvent> {
            chunks.forEach { with(pipeline) { process(it) } }
        }.toList()

    private suspend fun processOnce(pipeline: RecognitionPipeline) {
        flow<SpeechRecognitionEvent> { with(pipeline) { process(ByteArray(4)) } }.toList()
    }

    private companion object {
        /** 振幅 10000 の 16bit PCM。音量は閾値を超える。 */
        val loudPcm = pcmOf(amplitude = 10_000)

        /** 振幅 100 の 16bit PCM。音量は閾値未満。 */
        val quietPcm = pcmOf(amplitude = 100)

        fun pcmOf(amplitude: Int): ByteArray =
            ByteArray(4) { i -> if (i % 2 == 0) (amplitude and 0xFF).toByte() else (amplitude shr 8).toByte() }
    }
}
