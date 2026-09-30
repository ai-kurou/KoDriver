@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenWindowsSpeechSettingsTest {
    @Test
    fun `Windowsではms-settingsの音声設定をrundll32で開く`() {
        val commands = mutableListOf<List<String>>()

        openWindowsSpeechSettings(isWindows = true, startProcess = { commands += it })

        assertEquals(
            listOf(listOf("rundll32", "url.dll,FileProtocolHandler", "ms-settings:speech")),
            commands,
        )
    }

    @Test
    fun `Windows以外では何も起動しない`() {
        val commands = mutableListOf<List<String>>()

        openWindowsSpeechSettings(isWindows = false, startProcess = { commands += it })

        assertEquals(emptyList(), commands)
    }

    @Test
    fun `プロセス起動でIOExceptionが発生しても例外を伝播せずSentryへ記録する`() {
        val error = IOException("failed")
        val captured = mutableListOf<Throwable>()

        openWindowsSpeechSettings(
            isWindows = true,
            startProcess = { throw error },
            captureException = { captured += it },
        )

        assertEquals(listOf<Throwable>(error), captured)
    }

    @Test
    fun `プロセス起動でSecurityExceptionが発生しても例外を伝播せずSentryへ記録する`() {
        val error = SecurityException("denied")
        val captured = mutableListOf<Throwable>()

        openWindowsSpeechSettings(
            isWindows = true,
            startProcess = { throw error },
            captureException = { captured += it },
        )

        assertEquals(listOf<Throwable>(error), captured)
    }

    @Test
    fun `Windows以外では例外を記録しない`() {
        val captured = mutableListOf<Throwable>()

        openWindowsSpeechSettings(isWindows = false, startProcess = {}, captureException = { captured += it })

        assertEquals(emptyList(), captured)
    }
}
