package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.TextToSpeechVoice
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SapiSpeechVoiceListTest {
    private var output: Path? = null
    private val failures = mutableListOf<Throwable>()

    @AfterTest
    fun `一時ファイルを片付ける`() {
        output?.let { Files.deleteIfExists(it) }
    }

    @Test
    fun `音声一覧を解析して出力ファイルを削除する`() {
        val process = FakeSpeechCommandProcess()
        val synthesizer = synthesizer(process)
        assertEquals(listOf(TextToSpeechVoice("voice", "日本語", "ja-JP", true)), synthesizer.listVoices())
        assertTrue(failures.isEmpty())
        assertFalse(Files.exists(requireNotNull(output)))
        assertEquals(listOf("wait:15", "destroy"), process.calls)
    }

    @Test
    fun `異常終了を報告してファイルとプロセスを片付ける`() {
        val process = FakeSpeechCommandProcess(exitCode = 2)
        assertEquals(emptyList(), synthesizer(process).listVoices())
        assertEquals("音声一覧の取得に失敗しました（終了コード: 2）", failures.single().message)
        assertFalse(Files.exists(requireNotNull(output)))
        assertEquals(listOf("wait:15", "destroy"), process.calls)
    }

    @Test
    fun `タイムアウト後は強制終了を待ってから片付ける`() {
        val process = FakeSpeechCommandProcess(completes = false)
        assertEquals(emptyList(), synthesizer(process).listVoices())
        assertEquals("音声一覧の取得がタイムアウトしました", failures.single().message)
        assertEquals(listOf("wait:15", "force", "wait:5", "destroy"), process.calls)
        assertFalse(Files.exists(requireNotNull(output)))
    }

    @Test
    fun `起動失敗でも一時ファイルを削除する`() {
        val failure = IOException("起動失敗")
        val synthesizer =
            SapiSpeechSynthesizer(isWindows = true, startProcess = {
                output = it.redirectOutput().file().toPath()
                throw failure
            }, captureException = failures::add)
        assertEquals(emptyList(), synthesizer.listVoices())
        assertSame(failure, failures.single())
        assertFalse(Files.exists(requireNotNull(output)))
    }

    @Test
    fun `出力を読めなければ失敗を報告してプロセスを破棄する`() {
        val process = FakeSpeechCommandProcess()
        assertEquals(emptyList(), synthesizer(process, removeOutput = true).listVoices())
        assertEquals(1, failures.size)
        assertEquals(listOf("wait:15", "destroy"), process.calls)
    }

    private fun synthesizer(
        process: Process,
        removeOutput: Boolean = false,
    ): SapiSpeechSynthesizer =
        SapiSpeechSynthesizer(isWindows = true, startProcess = {
            assertEquals(buildListVoicesScript(), it.command().last())
            assertEquals(ProcessBuilder.Redirect.DISCARD, it.redirectError())
            val path = it.redirectOutput().file().toPath()
            output = path
            Files.writeString(path, "voice\t日本語\tja-JP\tTrue\n")
            if (removeOutput) Files.delete(path)
            process
        }, captureException = failures::add)
}
