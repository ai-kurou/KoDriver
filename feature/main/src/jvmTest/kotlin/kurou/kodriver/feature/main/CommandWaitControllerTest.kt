package kurou.kodriver.feature.main

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.SpeechRecognitionChime
import kurou.kodriver.domain.model.SpeechRecognitionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CommandWaitControllerTest {
    private val modes = mutableListOf<SpeechRecognitionMode>()
    private val chimes = mutableListOf<SpeechRecognitionChime>()
    private var started = 0
    private var ended = 0
    private var chimeError: Exception? = null

    private fun TestScope.controller() =
        CommandWaitController(
            scope = this,
            setMode = { modes += it },
            playChime = {
                chimeError?.let { e -> throw e }
                chimes += it
            },
            onCommandWaitStarted = { started++ },
            onCommandWaitEnded = { ended++ },
        )

    @Test
    fun `ウェイクワード待ちのモードを伝える`() =
        runTest {
            controller().applyMode()

            assertEquals(listOf(SpeechRecognitionMode.WakeWord), modes)
        }

    @Test
    fun `コマンド待ちを始めると上昇音を鳴らし、時間切れで下降音を鳴らして戻る`() =
        runTest {
            val controller = controller()

            controller.startCommandWait()
            runCurrent()
            assertTrue(controller.commandWaiting)
            assertEquals(listOf(SpeechRecognitionChime.WakeWordDetected), chimes)
            assertEquals(1, started)

            advanceTimeBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS)
            runCurrent()

            assertFalse(controller.commandWaiting)
            assertEquals(
                listOf(SpeechRecognitionChime.WakeWordDetected, SpeechRecognitionChime.CommandTimedOut),
                chimes,
            )
            assertEquals(listOf(SpeechRecognitionMode.Command, SpeechRecognitionMode.WakeWord), modes)
            assertEquals(1, ended)
        }

    @Test
    fun `時間切れ以外で終えたときは下降音を鳴らさず、時間切れも起きない`() =
        runTest {
            val controller = controller()
            controller.startCommandWait()
            runCurrent()

            controller.finishCommandWait()
            advanceTimeBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS * 2)
            runCurrent()

            assertEquals(listOf(SpeechRecognitionChime.WakeWordDetected), chimes)
            assertEquals(1, ended)
        }

    @Test
    fun `確認音を鳴らせなくてもコマンド待ちを続ける`() =
        runTest {
            chimeError = IllegalStateException("no audio")
            val controller = controller()

            controller.startCommandWait()
            runCurrent()

            assertTrue(controller.commandWaiting)
            assertEquals(emptyList(), chimes)
        }

    @Test
    fun `コマンド待ち中に読み上げが始まると打ち切って停止する`() =
        runTest {
            val controller = controller()
            controller.startCommandWait()

            controller.pause()
            advanceTimeBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS * 2)
            runCurrent()

            assertTrue(controller.paused)
            assertFalse(controller.commandWaiting)
            assertEquals(1, ended)
            assertEquals(listOf(SpeechRecognitionMode.Command, SpeechRecognitionMode.Paused), modes)
            assertEquals(emptyList(), chimes)
        }

    @Test
    fun `コマンド待ちでないときの読み上げ開始は表示を変えず、再開でウェイクワード待ちへ戻る`() =
        runTest {
            val controller = controller()

            controller.pause()
            controller.resume()

            assertFalse(controller.paused)
            assertEquals(0, ended)
            assertEquals(listOf(SpeechRecognitionMode.Paused, SpeechRecognitionMode.WakeWord), modes)
        }

    @Test
    fun `リセットすると状態とJobを破棄し、モードは伝えない`() =
        runTest {
            val controller = controller()
            controller.startCommandWait()
            controller.pause()
            modes.clear()

            controller.reset()
            advanceTimeBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS * 2)
            runCurrent()

            assertFalse(controller.paused)
            assertFalse(controller.commandWaiting)
            assertEquals(emptyList(), modes)
        }
}
