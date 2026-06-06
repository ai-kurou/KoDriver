package kurou.kodriver.tts

import kurou.kodriver.domain.engine.TextToSpeechEngine

internal class WindowsTtsEngine : TextToSpeechEngine {

    private val process: Process = ProcessBuilder(
        "powershell",
        "-NoProfile",
        "-NonInteractive",
        "-Command",
        """
        Add-Type -AssemblyName System.Speech
        ${'$'}tts = New-Object System.Speech.Synthesis.SpeechSynthesizer
        while (${'$'}true) {
            ${'$'}line = [Console]::ReadLine()
            if (${'$'}line -eq ${'$'}null) { break }
            ${'$'}tts.SpeakAsync(${'$'}line)
        }
        """.trimIndent(),
    ).start()

    private val writer = process.outputStream.bufferedWriter()

    override fun speak(text: String) {
        writer.write(text.replace("\n", " "))
        writer.newLine()
        writer.flush()
    }

    override fun stop() {
        process.destroy()
    }
}
