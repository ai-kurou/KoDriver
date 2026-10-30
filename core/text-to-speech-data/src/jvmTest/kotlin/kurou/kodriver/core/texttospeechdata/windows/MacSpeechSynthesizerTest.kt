package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.TextToSpeechVoice
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.concurrent.thread
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MacSpeechSynthesizerTest {
    private var output: Path? = null
    private val failures = mutableListOf<Throwable>()
    private val builders = mutableListOf<ProcessBuilder>()
    private val processes = mutableListOf<FakeSayProcess>()

    @AfterTest
    fun `一時ファイルを片付ける`() {
        output?.let { Files.deleteIfExists(it) }
        processes.forEach { it.finish() }
    }

    private fun voiceSynthesizer(
        process: FakeSpeechCommandProcess,
        text: String = "Kyoko   ja_JP    # こんにちは\n",
    ) = MacSpeechSynthesizer(isMac = true, startProcess = {
        output = it.redirectOutput().file().toPath()
        Files.writeString(requireNotNull(output), text)
        builders.add(it)
        process
    }, captureException = failures::add)

    private fun speakSynthesizer(
        startFailure: IOException? = null,
        exitCodes: List<Int> = emptyList(),
    ) = MacSpeechSynthesizer(isMac = true, startProcess = {
        startFailure?.let { f -> throw f }
        builders.add(it)
        FakeSayProcess(exitCodes.getOrElse(processes.size) { 0 }).also(processes::add)
    }, captureException = failures::add)

    private fun MacSpeechSynthesizer.say(
        text: String = "text",
        queue: Boolean = false,
        voiceId: String = "",
        speed: Float = 1f,
    ) = speak(text, queue, 50, voiceId, speed, 1.5f)

    @Test
    fun `音声一覧を取得して利用可能と判定する`() {
        val process = FakeSpeechCommandProcess()
        val synthesizer = voiceSynthesizer(process)
        assertEquals(listOf(TextToSpeechVoice("Kyoko", "Kyoko", "ja-JP", true)), synthesizer.listVoices())
        assertEquals(listOf("say", "-v", "?"), builders.single().command())
        assertFalse(Files.exists(requireNotNull(output)))
        assertEquals(listOf("wait:15", "destroy"), process.calls)
        assertTrue(synthesizer.isAvailable())
    }

    @Test
    fun `日本語音声が無ければ利用不可`() {
        assertFalse(voiceSynthesizer(FakeSpeechCommandProcess(), "Alex en_US # Hi\n").isAvailable())
    }

    @Test
    fun `macでない場合と音量0では何もしない`() {
        val synthesizer = MacSpeechSynthesizer(isMac = false, startProcess = { error("起動しない") })
        assertFalse(synthesizer.isAvailable())
        assertEquals(emptyList(), synthesizer.listVoices())
        synthesizer.say()
        speakSynthesizer().speak("text", false, 0, "", 1f, 1f)
        assertTrue(builders.isEmpty())
    }

    @Test
    fun `異常終了を報告する`() {
        assertEquals(emptyList(), voiceSynthesizer(FakeSpeechCommandProcess(exitCode = 2)).listVoices())
        assertEquals("音声一覧の取得に失敗しました（終了コード: 2）", failures.single().message)
        assertFalse(Files.exists(requireNotNull(output)))
    }

    @Test
    fun `タイムアウトを報告する`() {
        val process = FakeSpeechCommandProcess(completes = false)
        assertEquals(emptyList(), voiceSynthesizer(process).listVoices())
        assertEquals("音声一覧の取得がタイムアウトしました", failures.single().message)
        assertEquals(listOf("wait:15", "force", "wait:5", "destroy"), process.calls)
    }

    @Test
    fun `起動失敗を報告する`() {
        val failure = IOException("起動失敗")
        val synthesizer =
            MacSpeechSynthesizer(isMac = true, startProcess = {
                output = it.redirectOutput().file().toPath()
                throw failure
            }, captureException = failures::add)
        assertEquals(emptyList(), synthesizer.listVoices())
        assertSame(failure, failures.single())
        assertFalse(Files.exists(requireNotNull(output)))
    }

    @Test
    fun `sayコマンドの引数を組み立てて完了までブロックする`() {
        val synthesizer = speakSynthesizer()
        val worker = thread { synthesizer.say("-こんにちは", speed = 2f) }
        assertTrue(waitForProcess(0))
        assertEquals(listOf("say", "-v", "Kyoko", "-r", "350", "--", "-こんにちは"), builders.single().command())
        assertTrue(worker.isAlive)
        processes[0].finish()
        worker.join(5000)
        assertFalse(worker.isAlive)
    }

    @Test
    fun `指定音声で失敗したらKyokoで読み直す`() {
        val synthesizer = speakSynthesizer(exitCodes = listOf(1))
        val worker = thread { synthesizer.say(voiceId = "Eddy (日本語（日本）)") }
        assertTrue(waitForProcess(0))
        processes[0].finish()
        assertTrue(waitForProcess(1))
        assertEquals("Eddy (日本語（日本）)", builders[0].command()[2])
        assertEquals("Kyoko", builders[1].command()[2])
        processes[1].finish()
        worker.join(5000)
        assertTrue(failures.isEmpty())
    }

    @Test
    fun `Kyokoでも失敗したら報告する`() {
        val synthesizer = speakSynthesizer(exitCodes = listOf(1, 1))
        val worker = thread { synthesizer.say(voiceId = "Missing") }
        assertTrue(waitForProcess(0))
        processes[0].finish()
        assertTrue(waitForProcess(1))
        processes[1].finish()
        worker.join(5000)
        assertEquals(2, builders.size)
        assertEquals("sayが失敗しました（終了コード: 1）", failures.single().message)
    }

    @Test
    fun `キューなしは再生中のプロセスを打ち切る`() {
        val synthesizer = speakSynthesizer()
        val first = thread { synthesizer.say("1") }
        assertTrue(waitForProcess(0))
        val second = thread { synthesizer.say("2") }
        assertTrue(waitForProcess(1))
        first.join(5000)
        assertTrue(processes[0].destroyed)
        assertFalse(processes[1].destroyed)
        processes[1].finish()
        second.join(5000)
    }

    @Test
    fun `キューありは再生中の終了を待ってから開始する`() {
        val synthesizer = speakSynthesizer()
        val first = thread { synthesizer.say("1") }
        assertTrue(waitForProcess(0))
        val second = thread { synthesizer.say("2", queue = true) }
        Thread.sleep(200)
        assertEquals(1, builders.size)
        processes[0].finish()
        assertTrue(waitForProcess(1))
        assertFalse(processes[0].destroyed)
        processes[1].finish()
        first.join(5000)
        second.join(5000)
        assertFalse(second.isAlive)
    }

    @Test
    fun `待機中に追い越されたら再生しない`() {
        val synthesizer = speakSynthesizer()
        val first = thread { synthesizer.say("1") }
        assertTrue(waitForProcess(0))
        val queued = thread { synthesizer.say("2", queue = true) }
        Thread.sleep(200)
        val overtaker = thread { synthesizer.say("3") }
        assertTrue(waitForProcess(1))
        queued.join(5000)
        assertFalse(queued.isAlive)
        assertEquals(2, builders.size)
        assertEquals("3", builders[1].command().last())
        processes[1].finish()
        first.join(5000)
        overtaker.join(5000)
    }

    @Test
    fun `割り込まれたら自分のプロセスを終了して再送出する`() {
        val synthesizer = speakSynthesizer()
        var interrupted = false
        val worker =
            thread {
                try {
                    synthesizer.say()
                } catch (e: InterruptedException) {
                    interrupted = true
                }
            }
        assertTrue(waitForProcess(0))
        worker.interrupt()
        worker.join(5000)
        assertTrue(interrupted)
        assertTrue(processes[0].destroyed)
    }

    @Test
    fun `起動失敗は報告して戻る`() {
        val failure = IOException("起動失敗")
        speakSynthesizer(failure).say()
        assertSame(failure, failures.single())
    }

    private fun waitForProcess(index: Int): Boolean {
        repeat(100) {
            if (processes.size > index) return processes[index].awaitWaiting()
            Thread.sleep(50)
        }
        return false
    }
}
