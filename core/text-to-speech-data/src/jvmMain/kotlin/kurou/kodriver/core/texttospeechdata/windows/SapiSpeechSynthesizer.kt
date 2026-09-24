package kurou.kodriver.core.texttospeechdata.windows

/**
 * Windows標準の音声合成（SAPI）をPowerShellの`System.Speech.Synthesis.SpeechSynthesizer`経由で
 * 呼び出す [WindowsSpeechSynthesizer] の実装。
 *
 * JVMには音声合成の標準APIが無く、SAPIのCOMインターフェースをJNAで直接叩くには
 * 型ライブラリのバインディングが必要になるため、1回の読み上げごとにPowerShellプロセスを
 * 起動する方式を採る。読み上げの中断（[stop]）はプロセスの破棄で行う。
 *
 * Windows専用の外部プロセスを起動するためユニットテストの対象外とし、
 * 読み上げ制御のロジックは [WindowsSpeechSynthesizer] を差し替えられる呼び出し側で検証する。
 */
internal class SapiSpeechSynthesizer : WindowsSpeechSynthesizer {
    private val lock = Any()
    private var process: Process? = null

    override fun isAvailable(): Boolean = IS_WINDOWS

    override fun speak(
        text: String,
        queue: Boolean,
    ) {
        if (!IS_WINDOWS) return
        synchronized(lock) {
            if (queue) process?.waitFor() else destroyProcess()
            process =
                ProcessBuilder(POWERSHELL, "-NoProfile", "-NonInteractive", "-Command", buildScript(text))
                    .redirectErrorStream(true)
                    .start()
        }
    }

    override fun stop() {
        synchronized(lock) { destroyProcess() }
    }

    private fun destroyProcess() {
        process?.destroy()
        process = null
    }

    /** PowerShellの単一引用符文字列へ埋め込むため、テキスト中の`'`を`''`へエスケープする。 */
    private fun buildScript(text: String): String {
        val escaped = text.replace("'", "''")
        return "Add-Type -AssemblyName System.Speech; " +
            "(New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('$escaped')"
    }

    private companion object {
        const val POWERSHELL = "powershell.exe"
        val IS_WINDOWS = System.getProperty("os.name").lowercase().startsWith("windows")
    }
}
