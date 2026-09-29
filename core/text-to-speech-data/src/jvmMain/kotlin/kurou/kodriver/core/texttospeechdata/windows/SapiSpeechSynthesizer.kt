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
 *
 * `queue = true` で前の発話の終了を待つ間は [lock] を保持しない。保持したままだと、待っている間に
 * 呼ばれた [stop] や別スレッドからの `queue = false` の [speak] 呼び出しが [lock] を取得できず、
 * 前の発話が自然に終わるまで割り込めなくなってしまう（`runInterruptible` によるコルーチンの
 * キャンセル伝播の意味がなくなる）。ただし [lock] を保持しない間に別スレッドの [speak] 呼び出しが
 * 割り込んでいる可能性があるため、待機後は [requestToken] で自分がまだ最新の要求かを確認し、
 * 既に別の呼び出しに追い越されていれば新しいプロセスは起動しない（起動すると音声が重なってしまう）。
 */
internal class SapiSpeechSynthesizer : WindowsSpeechSynthesizer {
    private val lock = Any()
    private var process: Process? = null
    private var requestToken = 0L

    override fun isAvailable(): Boolean {
        if (!IS_WINDOWS) return false
        return try {
            val process =
                ProcessBuilder(
                    POWERSHELL,
                    "-NoProfile",
                    "-NonInteractive",
                    "-Command",
                    "Add-Type -AssemblyName System.Speech; " +
                        "\$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "\$voices = \$s.GetInstalledVoices(); " +
                        "if (-not (\$voices | Where-Object { " +
                        "\$_.VoiceInfo.Culture.Name -eq 'ja-JP' })) { exit 1 }",
                ).redirectErrorStream(true).start()
            try {
                process.inputStream.bufferedReader().use { it.readText() }
                process.waitFor() == 0
            } finally {
                process.destroy()
            }
        } catch (_: Exception) {
            false
        }
    }

    override fun speak(
        text: String,
        queue: Boolean,
    ) {
        if (!IS_WINDOWS) return
        val token = synchronized(lock) { ++requestToken }
        if (queue) {
            awaitInterruptibly(synchronized(lock) { process })
        } else {
            synchronized(lock) { destroyProcess() }
        }
        val started =
            synchronized(lock) {
                // 待機中に別スレッドの新しい呼び出しへ追い越されていたら、今さら発話を開始しない。
                if (token != requestToken) return
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

    /** [lock] を保持せずに [target] の終了を待つ。割り込まれた場合は、まだ現在の発話であれば破棄する。 */
    private fun awaitInterruptibly(target: Process?) {
        try {
            target?.waitFor()
        } catch (e: InterruptedException) {
            synchronized(lock) { if (process === target) destroyProcess() }
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
