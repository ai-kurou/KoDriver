package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecognitionPipelineTest {
    private val modes = mutableListOf<SpeechRecognitionMode>()
    private val results = ArrayDeque<SpeechTranscript?>()
    private val transcriber =
        object : SpeechTranscriber {
            override fun accept(pcm: ByteArray): SpeechTranscript? = results.removeFirst()

            override fun setMode(mode: SpeechRecognitionMode) {
                modes += mode
            }

            override fun close() = Unit
        }
    private val mode = MutableStateFlow(SpeechRecognitionMode.WakeWord)
    private val pipeline = RecognitionPipeline(transcriber, mode)

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
            processOnce(RecognitionPipeline(transcriber, modeSync))
            mode.value = SpeechRecognitionMode.WakeWord
            processOnce(RecognitionPipeline(transcriber, modeSync))

            assertEquals(listOf(SpeechRecognitionMode.Paused, SpeechRecognitionMode.WakeWord), modes)
        }

    private suspend fun processOnce(pipeline: RecognitionPipeline) {
        flow<SpeechRecognitionEvent> { with(pipeline) { process(ByteArray(4)) } }.toList()
    }
}
