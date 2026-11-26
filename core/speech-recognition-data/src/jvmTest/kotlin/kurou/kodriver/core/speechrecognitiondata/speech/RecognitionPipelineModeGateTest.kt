package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionEvent
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionMode
import kotlin.test.Test
import kotlin.test.assertContentEquals

/** 音量ゲートをモードごとに適用するかどうか（ウェイクワード検出中・コマンド受付中・読み上げ中）を確かめる。 */
class RecognitionPipelineModeGateTest {
    private val accepted = mutableListOf<ByteArray>()
    private val transcriber =
        object : SpeechTranscriber {
            override fun accept(pcm: ByteArray): SpeechTranscript? {
                accepted += pcm
                return null
            }

            override fun setMode(mode: SpeechRecognitionMode) = Unit

            override fun close() = Unit
        }
    private val mode = MutableStateFlow(SpeechRecognitionMode.WakeWord)
    private val threshold = MutableStateFlow(0.3f)
    private val cutInCommand = MutableStateFlow(false)
    private val pipeline = RecognitionPipeline(transcriber, mode, threshold, cutInCommand)

    @Test
    fun `コマンド受付中は閾値未満の断片も元の音声を認識器に渡す`() =
        runTest {
            mode.value = SpeechRecognitionMode.Command

            feed(quietPcm)

            assertContentEquals(quietPcm, accepted.single())
        }

    @Test
    fun `コマンド受付中もカットする設定なら、閾値未満の断片は全ゼロを渡す`() =
        runTest {
            mode.value = SpeechRecognitionMode.Command
            cutInCommand.value = true

            feed(quietPcm)

            assertContentEquals(ByteArray(quietPcm.size), accepted.single())
        }

    @Test
    fun `コマンド受付中もカットする設定なら、閾値以上の断片は元の音声を渡す`() =
        runTest {
            mode.value = SpeechRecognitionMode.Command
            cutInCommand.value = true

            feed(loudPcm)

            assertContentEquals(loudPcm, accepted.single())
        }

    @Test
    fun `読み上げ中は、コマンド受付中もカットする設定でも元の音声を渡す`() =
        runTest {
            mode.value = SpeechRecognitionMode.Paused
            cutInCommand.value = true

            feed(quietPcm)

            assertContentEquals(quietPcm, accepted.single())
        }

    @Test
    fun `コマンド受付中もカットする設定を聞き取りの途中で変えると次の断片から反映する`() =
        runTest {
            mode.value = SpeechRecognitionMode.Command

            flow<SpeechRecognitionEvent> {
                with(pipeline) { process(quietPcm) }
                cutInCommand.value = true
                with(pipeline) { process(quietPcm) }
            }.toList()

            assertContentEquals(quietPcm, accepted[0])
            assertContentEquals(ByteArray(quietPcm.size), accepted[1])
        }

    private suspend fun feed(vararg chunks: ByteArray) {
        flow<SpeechRecognitionEvent> {
            chunks.forEach { with(pipeline) { process(it) } }
        }.toList()
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
