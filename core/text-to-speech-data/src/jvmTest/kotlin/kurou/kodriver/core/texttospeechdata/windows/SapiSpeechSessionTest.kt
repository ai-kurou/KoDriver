package kurou.kodriver.core.texttospeechdata.windows

import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class SapiSpeechSessionTest {
    private val processes = mutableListOf<FakeResidentSpeechProcess>()

    @AfterTest
    fun `読み取りスレッドを終了する`() {
        processes.forEach { it.finishAndJoinReader() }
    }

    @Test
    fun `ウォームアップで常駐プロセスを起動して再利用する`() {
        val synthesizer =
            SapiSpeechSynthesizer(isWindows = true, startProcess = {
                assertEquals(
                    listOf(
                        "powershell.exe",
                        "-NoProfile",
                        "-NonInteractive",
                        "-EncodedCommand",
                        encodeResidentSpeakScript(),
                    ),
                    it.command(),
                )
                assertEquals(true, it.redirectErrorStream())
                newProcess()
            })
        synthesizer.warmUp()
        synthesizer.warmUp()
        assertEquals(1, processes.size)
    }

    @Test
    fun `常駐プロセスが終了していたら起動し直す`() {
        val synthesizer = SapiSpeechSynthesizer(isWindows = true, startProcess = { newProcess() })
        synthesizer.warmUp()
        processes.single().destroy()
        synthesizer.warmUp()
        assertEquals(2, processes.size)
    }

    @Test
    fun `起動失敗を報告して次回に再試行する`() {
        val failure = IOException("起動失敗")
        val failures = mutableListOf<Throwable>()
        var attempts = 0
        val synthesizer =
            SapiSpeechSynthesizer(isWindows = true, startProcess = {
                if (++attempts == 1) throw failure
                newProcess()
            }, captureException = failures::add)
        synthesizer.warmUp()
        synthesizer.warmUp()
        assertSame(failure, failures.single())
        assertEquals(2, attempts)
        assertEquals(1, processes.size)
    }

    @Test
    fun `発話時の起動失敗は完了待機を行わない`() {
        val failure = IOException("起動失敗")
        val failures = mutableListOf<Throwable>()
        val synthesizer =
            SapiSpeechSynthesizer(
                isWindows = true,
                startProcess = { throw failure },
                captureException = failures::add,
                awaitCompletion = { error("待機してはいけません") },
            )
        synthesizer.speak("本文", false, 80, "voice", 1f, 1f)
        assertSame(failure, failures.single())
    }

    @Test
    fun `発話パラメータを送信して応答まで待つ`() {
        val process = newProcess()
        val synthesizer = SapiSpeechSynthesizer(isWindows = true, startProcess = { process })
        val call = SpeechCall { synthesizer.speak("本文", true, 80, "voice", 1.5f, 1.2f) }
        try {
            assertEquals(buildSpeakRequest("本文", 80, "voice", speedToSapiRate(1.5f), 1.2f), process.awaitRequest())
            process.respond("DONE")
            call.awaitSuccess()
        } finally {
            process.finishAndJoinReader()
            call.join()
        }
    }

    private fun newProcess(): FakeResidentSpeechProcess = FakeResidentSpeechProcess().also { processes.add(it) }
}
