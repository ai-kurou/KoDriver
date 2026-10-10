package kurou.kodriver.core.texttospeechdata.windows

import io.sentry.Sentry
import kurou.kodriver.domain.model.TTS_CULTURE_NAME
import kurou.kodriver.domain.model.TextToSpeechVoice
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Windows標準の音声合成（SAPI）をPowerShellの`System.Speech.Synthesis.SpeechSynthesizer`経由で
 * 呼び出す [WindowsSpeechSynthesizer] の実装。
 *
 * JVMには音声合成の標準APIが無く、SAPIのCOMインターフェースをJNAで直接叩くには
 * 型ライブラリのバインディングが必要になるため、PowerShellプロセスを1つ常駐させて使い回す。
 * 読み上げのたびにプロセスを起動すると、PowerShellと.NETの起動・`System.Speech`のロード・
 * `SpeechSynthesizer`の生成が毎回かかって発話が遅れるため、これらは最初の1回（[warmUp] または最初の [speak]）
 * だけにして、以降は標準入力へ要求行を送るだけにする（[ResidentSpeechSession]、[buildResidentSpeakScript]）。
 * [speak] は読み上げの完了（応答行の受信）まで [CompletableFuture.get] でブロックする。
 * 呼び出し元（[WindowsTextToSpeechRepository]）は `runInterruptible` でこの呼び出しを包んでおり、
 * コルーチンのキャンセルはスレッド割り込み → [InterruptedException] としてここへ伝わるため、
 * その際は読み上げの打ち切り要求を送ってから例外を再送出する。明示的な中断（[stop]）も同じ要求で行う。
 * 常駐プロセスが終了した場合は、次の [speak] で起動し直す。アプリ終了時は標準入力が閉じられて常駐プロセスも終了する。
 *
 * Windows専用の外部プロセスを起動するためユニットテストの対象外とし、
 * 読み上げ制御のロジックは [WindowsSpeechSynthesizer] を差し替えられる呼び出し側で検証する。
 *
 * `queue = true` で前の発話の終了を待つ間は [lock] を保持しない。保持したままだと、待っている間に
 * 呼ばれた [stop] や別スレッドからの `queue = false` の [speak] 呼び出しが [lock] を取得できず、
 * 前の発話が自然に終わるまで割り込めなくなってしまう（`runInterruptible` によるコルーチンの
 * キャンセル伝播の意味がなくなる）。ただし [lock] を保持しない間に別スレッドの [speak] 呼び出しが
 * 割り込んでいる可能性があるため、待機後は [requestToken] で自分がまだ最新の要求かを確認し、
 * 既に別の呼び出しに追い越されていれば要求を送らない。
 */
internal class SapiSpeechSynthesizer : WindowsSpeechSynthesizer {
    private val lock = Any()
    private var session: ResidentSpeechSession? = null
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
                        "\$_.Enabled -and \$_.VoiceInfo.Culture.Name -eq '$TTS_CULTURE_NAME' })) { exit 1 }",
                ).redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start()
            try {
                if (!process.waitFor(AVAILABILITY_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    process.destroyForcibly()
                    return false
                }
                process.exitValue() == 0
            } finally {
                process.destroy()
            }
        } catch (_: Exception) {
            false
        }
    }

    override fun listVoices(): List<TextToSpeechVoice> {
        if (!IS_WINDOWS) return emptyList()
        return try {
            // 終了待ち中に標準出力のバッファが満杯になるのを避けるため、一時ファイルへ出力する。
            val output = Files.createTempFile("kodriver-voices-", ".txt")
            try {
                readVoices(output)
            } finally {
                Files.deleteIfExists(output)
            }
        } catch (e: Exception) {
            // 取得失敗は音声が一つも表示されない原因になるため、握り潰さずSentryへ報告する。
            Sentry.captureException(e)
            emptyList()
        }
    }

    private fun readVoices(output: Path): List<TextToSpeechVoice> {
        val process =
            ProcessBuilder(
                POWERSHELL,
                "-NoProfile",
                "-NonInteractive",
                "-Command",
                buildListVoicesScript(),
            ).redirectError(ProcessBuilder.Redirect.DISCARD)
                .redirectOutput(output.toFile())
                .start()
        try {
            if (!process.waitFor(AVAILABILITY_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                // 出力ファイルを掴んだまま残ると呼び出し元の削除に失敗するため、終了を待ってから戻る。
                process.destroyForcibly().waitFor(DESTROY_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                error("音声一覧の取得がタイムアウトしました")
            }
            check(process.exitValue() == 0) { "音声一覧の取得に失敗しました（終了コード: ${process.exitValue()}）" }
            return parseVoiceList(Files.readString(output, Charsets.UTF_8))
        } finally {
            process.destroy()
        }
    }

    override fun warmUp() {
        if (!IS_WINDOWS) return
        synchronized(lock) { usableSession() }
    }

    override fun speak(
        text: String,
        queue: Boolean,
        volume: Int,
        voiceId: String,
        speed: Float,
    ) {
        if (!IS_WINDOWS) return
        val token = synchronized(lock) { ++requestToken }
        val request = buildSpeakRequest(text, volume, voiceId, speedToSapiRate(speed))
        val future = enqueue(token, request, queue) ?: return
        await(future)
    }

    /**
     * [request] を常駐プロセスへ送り、その完了を表す [CompletableFuture] を返す。
     * [queue] が `true` で読み上げ中なら、その完了を [lock] の外で待ってから送り直す。
     * 待機中に新しい呼び出しへ追い越された場合、またはプロセスを起動できない場合は `null` を返す。
     */
    private fun enqueue(
        token: Long,
        request: String,
        queue: Boolean,
    ): CompletableFuture<Unit>? {
        while (true) {
            val blocker =
                synchronized(lock) {
                    if (token != requestToken) return null
                    val current = usableSession() ?: return null
                    if (!queue) current.stop()
                    val last = if (queue) current.lastOutstanding() else null
                    if (last == null) return current.send(request)
                    last
                }
            await(blocker)
        }
    }

    /** [lock] を保持せずに [target] の完了を待つ。割り込まれた場合は、まだ現在の読み上げであれば打ち切る。 */
    private fun await(target: CompletableFuture<Unit>) {
        try {
            target.get()
        } catch (e: InterruptedException) {
            synchronized(lock) { session?.stopIfCurrent(target) }
            throw e
        }
    }

    override fun stop() {
        synchronized(lock) { session?.stop() }
    }

    /** 常駐プロセスが使えなければ起動し直す。起動に失敗したら `null`。呼び出し元が [lock] を保持していること。 */
    private fun usableSession(): ResidentSpeechSession? {
        session?.takeIf { it.isUsable }?.let { return it }
        return try {
            val process =
                ProcessBuilder(
                    POWERSHELL,
                    "-NoProfile",
                    "-NonInteractive",
                    "-EncodedCommand",
                    encodeResidentSpeakScript(),
                ).redirectErrorStream(true).start()
            ResidentSpeechSession(process, lock).also { session = it }
        } catch (e: IOException) {
            Sentry.captureException(e)
            null
        }
    }

    private companion object {
        const val POWERSHELL = "powershell.exe"

        /** 初回の`Add-Type`は低スペック環境やウイルス対策ソフトの影響で遅くなるため、誤検知を避けて余裕を持たせる。 */
        const val AVAILABILITY_CHECK_TIMEOUT_SECONDS = 15L

        /** タイムアウトで強制終了したプロセスの終了を待つ上限。 */
        const val DESTROY_WAIT_TIMEOUT_SECONDS = 5L
        val IS_WINDOWS = System.getProperty("os.name").lowercase().startsWith("windows")
    }
}
