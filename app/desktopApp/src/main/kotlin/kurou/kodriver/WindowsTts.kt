package kurou.kodriver

object WindowsTts {
    fun speak(text: String) {
        if (!System.getProperty("os.name").contains("Windows", ignoreCase = true)) return
        ProcessBuilder(
            "powershell.exe", "-Command",
            "Add-Type -AssemblyName System.Speech; " +
                "\$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                "\$s.Speak('$text')",
        )
            .redirectErrorStream(true)
            .start()
    }
}
