package kurou.kodriver.core.speechrecognitiondata.speech

import kurou.kodriver.domain.model.SpeechRecognitionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ModeSwitchingTranscriberTest {
    private val calls = mutableListOf<String>()

    private inner class FakeRecognizer(
        private val name: String,
        private val finished: Boolean = false,
    ) : VoskRecognizerPort {
        override fun setMaxAlternatives(count: Int) {
            calls += "$name.alternatives=$count"
        }

        override fun acceptWaveForm(
            pcm: ByteArray,
            size: Int,
        ): Boolean {
            calls += "$name.accept=$size"
            return finished
        }

        override fun result(): String = """{"alternatives":[{"text":"$name","confidence":1.0}]}"""

        override fun partialResult(): String = """{"partial":"$name"}"""

        override fun reset() {
            calls += "$name.reset"
        }

        override fun close() {
            calls += "$name.close"
        }
    }

    private fun transcriber(commandFinished: Boolean = false) =
        ModeSwitchingTranscriber(
            commandRecognizer = FakeRecognizer("command", commandFinished),
            wakeWordRecognizer = FakeRecognizer("wake"),
            closeModel = { calls += "model.close" },
        )

    @Test
    fun `作成時に両方の認識器の候補数を指定し、ウェイクワード待ちで始まる`() {
        val transcriber = transcriber()

        assertEquals(listOf("command.alternatives=5", "wake.alternatives=5"), calls)
        assertEquals(SpeechTranscript.Partial("wake"), transcriber.accept(ByteArray(4)))
    }

    @Test
    fun `コマンド待ちに切り替えると途中経過を捨ててコマンド用の認識器で認識する`() {
        val transcriber = transcriber(commandFinished = true)
        calls.clear()

        transcriber.setMode(SpeechRecognitionMode.Command)

        assertEquals(SpeechTranscript.Final(listOf("command")), transcriber.accept(ByteArray(4)))
        assertEquals(listOf("command.reset", "command.accept=4"), calls)
    }

    @Test
    fun `読み上げ中は音声を捨て、ウェイクワード待ちへ戻すと途中経過を捨てて認識する`() {
        val transcriber = transcriber()
        calls.clear()

        transcriber.setMode(SpeechRecognitionMode.Paused)
        assertNull(transcriber.accept(ByteArray(4)))
        transcriber.setMode(SpeechRecognitionMode.WakeWord)
        assertEquals(SpeechTranscript.Partial("wake"), transcriber.accept(ByteArray(4)))

        assertEquals(listOf("wake.reset", "wake.accept=4"), calls)
    }

    @Test
    fun `閉じるときは認識器を閉じてからモデルを閉じる`() {
        val transcriber = transcriber()
        calls.clear()

        transcriber.close()

        assertEquals(listOf("wake.close", "command.close", "model.close"), calls)
    }
}
