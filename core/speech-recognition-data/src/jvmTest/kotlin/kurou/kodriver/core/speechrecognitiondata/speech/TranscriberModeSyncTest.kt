package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.model.SpeechRecognitionMode
import kotlin.test.Test
import kotlin.test.assertEquals

class TranscriberModeSyncTest {
    private val modes = mutableListOf<SpeechRecognitionMode>()
    private val transcriber =
        object : SpeechTranscriber {
            override fun accept(pcm: ByteArray): SpeechTranscript? = null

            override fun setMode(mode: SpeechRecognitionMode) {
                modes += mode
            }

            override fun close() = Unit
        }
    private val state = MutableStateFlow(SpeechRecognitionMode.WakeWord)
    private val sync = TranscriberModeSync(transcriber, state)

    @Test
    fun `初期状態のウェイクワード待ちは切り替えない`() {
        assertEquals(SpeechRecognitionMode.WakeWord, sync.sync())
        assertEquals(emptyList(), modes)
    }

    @Test
    fun `モードが変わったときだけ切り替え、反映したモードを返す`() {
        state.value = SpeechRecognitionMode.Command

        assertEquals(SpeechRecognitionMode.Command, sync.sync())
        assertEquals(SpeechRecognitionMode.Command, sync.sync())
        assertEquals(listOf(SpeechRecognitionMode.Command), modes)
    }

    @Test
    fun `元のモードへ戻したときも切り替える`() {
        state.value = SpeechRecognitionMode.Paused
        sync.sync()
        state.value = SpeechRecognitionMode.WakeWord

        assertEquals(SpeechRecognitionMode.WakeWord, sync.sync())
        assertEquals(listOf(SpeechRecognitionMode.Paused, SpeechRecognitionMode.WakeWord), modes)
    }
}
