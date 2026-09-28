package kurou.kodriver.core.texttospeechdata.windows

/**
 * Windows標準の音声合成（SAPI）をPowerShellの`System.Speech.Synthesis.SpeechSynthesizer`経由で
 * 呼び出す [WindowsSpeechSynthesizer] の実装。
 *
 * JVMには音声合成の標準APIが無く、SAPIのCOMインターフェースをJNAで直接叩くには
 * 型ライブラリのバインディングが必要になるため、1回の読み上げごとにPowerShellプロセスを
 * 起動する方式を採る。[speak] はプロセスの終了（＝読み上げ完了）まで [Process.waitFor] で
 * ブロックする。呼び出し元（[WindowsTextToSpeechRepository]）は `runInterruptible` でこの呼び出しを
 * 包んでおり、コルーチンのキャンセルはスレッド割り込み → [InterruptedException] としてここへ伝わるため、
 * その際はプロセスを破棄（[destroyProcess]）してから例外を再送出し、読み上げを実際に打ち切る。
 * 読み上げの明示的な中断（[stop]）も同じくプロセスの破棄で行う。
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
        val started =
            synchronized(lock) {
                if (queue) process?.waitFor() else destroyProcess()
                val newProcess =
                    ProcessBuilder(POWERSHELL, "-NoProfile", "-NonInteractive", "-Command", buildScript(text))
                        .redirectErrorStream(true)
                        .start()
                process = newProcess
                newProcess
            }
        try {
            started.waitFor()
        } catch (e: InterruptedException) {
            synchronized(lock) { if (process === started) destroyProcess() }
            throw e
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
