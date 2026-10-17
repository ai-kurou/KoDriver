package kurou.kodriver.core.texttospeechdata.windows

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SapiSpeechAvailabilityTest {
    @Test
    fun `既定のOS判定は実行環境に従う`() {
        val synthesizer = SapiSpeechSynthesizer(startProcess = { FakeSpeechCommandProcess() })
        assertEquals(System.getProperty("os.name").lowercase().startsWith("windows"), synthesizer.isAvailable())
    }

    @Test
    fun `既定のエラー報告はSentryに例外を渡す`() {
        val failure = IOException("起動失敗")
        mockkStatic(Sentry::class)
        try {
            every { Sentry.captureException(failure) } returns SentryId.EMPTY_ID
            SapiSpeechSynthesizer(isWindows = true, startProcess = { throw failure }).warmUp()
            verify(exactly = 1) { Sentry.captureException(failure) }
            confirmVerified(Sentry::class)
        } finally {
            unmockkStatic(Sentry::class)
        }
    }

    @Test
    fun `Windows以外ではプロセスを起動しない`() {
        val synthesizer = SapiSpeechSynthesizer(isWindows = false, startProcess = { error("起動してはいけません") })
        assertFalse(synthesizer.isAvailable())
        assertEquals(emptyList(), synthesizer.listVoices())
        synthesizer.warmUp()
        synthesizer.speak("読み上げ", false, 100, "", 1f, 1f)
    }

    @Test
    fun `日本語音声の確認コマンドが成功したら利用できる`() {
        val process = FakeSpeechCommandProcess()
        val synthesizer =
            SapiSpeechSynthesizer(isWindows = true, startProcess = {
                assertEquals(
                    listOf("powershell.exe", "-NoProfile", "-NonInteractive", "-Command"),
                    it.command().take(4),
                )
                assertTrue(it.command().last().contains("VoiceInfo.Culture.Name -eq 'ja-JP'"))
                assertTrue(it.redirectErrorStream())
                assertEquals(ProcessBuilder.Redirect.DISCARD, it.redirectOutput())
                process
            })
        assertTrue(synthesizer.isAvailable())
        assertEquals(listOf("wait:15", "destroy"), process.calls)
    }

    @Test
    fun `終了コードが非ゼロなら利用できない`() {
        val process = FakeSpeechCommandProcess(exitCode = 1)
        assertFalse(SapiSpeechSynthesizer(isWindows = true, startProcess = { process }).isAvailable())
        assertEquals(listOf("wait:15", "destroy"), process.calls)
    }

    @Test
    fun `タイムアウトしたら強制終了して利用不可を返す`() {
        val process = FakeSpeechCommandProcess(completes = false)
        assertFalse(SapiSpeechSynthesizer(isWindows = true, startProcess = { process }).isAvailable())
        assertEquals(listOf("wait:15", "force", "destroy"), process.calls)
    }

    @Test
    fun `起動に失敗したら利用不可を返す`() {
        assertFalse(SapiSpeechSynthesizer(isWindows = true, startProcess = { throw IOException("起動失敗") }).isAvailable())
    }

    @Test
    fun `終了待機に失敗してもプロセスを破棄する`() {
        val process = FakeSpeechCommandProcess(waitFailure = IOException("待機失敗"))
        assertFalse(SapiSpeechSynthesizer(isWindows = true, startProcess = { process }).isAvailable())
        assertEquals(listOf("wait:15", "destroy"), process.calls)
    }
}
