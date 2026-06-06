package kurou.kodriver.tts

import kurou.kodriver.domain.engine.TextToSpeechEngine

internal class WindowsTtsEngine : TextToSpeechEngine {
    override fun speak(text: String) {
        val safeText = text.replace("'", "''")
        ProcessBuilder(
            "powershell",
            "-NoProfile",
            "-NonInteractive",
            "-Command",
            "Add-Type -AssemblyName System.Speech; " +
                "\$tts = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                "\$tts.Speak('$safeText')",
        ).start()
    }

    override fun stop() = Unit
}
