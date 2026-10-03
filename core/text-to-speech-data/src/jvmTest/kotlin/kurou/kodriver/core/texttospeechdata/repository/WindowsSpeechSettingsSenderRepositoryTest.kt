package kurou.kodriver.core.texttospeechdata.repository

import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowsSpeechSettingsSenderRepositoryTest {
    @Test
    fun `Windowsではms-settingsの音声設定をrundll32で開く`() {
        val commands = mutableListOf<List<String>>()

        WindowsSpeechSettingsSenderRepository(
            isWindows = true,
            startProcess = { commands += it },
        ).openWindowsSpeechSettings()

        assertEquals(
            listOf(listOf("rundll32", "url.dll,FileProtocolHandler", "ms-settings:speech")),
            commands,
        )
    }

    @Test
    fun `Windows以外では何も起動しない`() {
        val commands = mutableListOf<List<String>>()

        WindowsSpeechSettingsSenderRepository(
            isWindows = false,
            startProcess = { commands += it },
        ).openWindowsSpeechSettings()

        assertEquals(emptyList(), commands)
    }

    @Test
    fun `プロセス起動でIOExceptionが発生しても例外を伝播せずSentryへ記録する`() {
        val error = IOException("failed")
        val captured = mutableListOf<Throwable>()

        WindowsSpeechSettingsSenderRepository(
            isWindows = true,
            startProcess = { throw error },
            captureException = { captured += it },
        ).openWindowsSpeechSettings()

        assertEquals(listOf<Throwable>(error), captured)
    }

    @Test
    fun `プロセス起動でSecurityExceptionが発生しても例外を伝播せずSentryへ記録する`() {
        val error = SecurityException("denied")
        val captured = mutableListOf<Throwable>()

        WindowsSpeechSettingsSenderRepository(
            isWindows = true,
            startProcess = { throw error },
            captureException = { captured += it },
        ).openWindowsSpeechSettings()

        assertEquals(listOf<Throwable>(error), captured)
    }

    @Test
    fun `Windows以外では例外を記録しない`() {
        val captured = mutableListOf<Throwable>()

        WindowsSpeechSettingsSenderRepository(
            isWindows = false,
            startProcess = {},
            captureException = { captured += it },
        ).openWindowsSpeechSettings()

        assertEquals(emptyList(), captured)
    }
}
