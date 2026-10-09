package kurou.kodriver.core.texttospeechdata.windows

import io.sentry.Sentry
import java.io.IOException
import java.util.concurrent.CompletableFuture

/**
 * 常駐させたPowerShellプロセス1つ分との読み上げのやり取り。
 *
 * 要求は標準入力へ1行ずつ送り、読み上げの完了は標準出力の [SPEAK_RESPONSE_DONE] 行で受け取る。
 * 常駐スクリプトは要求を順番に処理するため、送った要求ごとの [CompletableFuture] を送信順に保持し、
 * 応答行が来るたびに先頭から完了させる。プロセスが終了（標準出力のEOF）または書き込みに失敗したら、
 * 待っている要求をすべて完了させて以降は使えない状態にする。
 *
 * 排他は呼び出し元（[SapiSpeechSynthesizer]）が保持する [lock] と共有する。
 * Windows専用の外部プロセスを起動するためユニットテストの対象外。
 */
internal class ResidentSpeechSession(
    private val process: Process,
    private val lock: Any,
) {
    private val outstanding = ArrayDeque<CompletableFuture<Unit>>()
    private var closed = false

    val isUsable: Boolean
        get() = synchronized(lock) { !closed } && process.isAlive

    init {
        Thread(::readResponses, "kodriver-speech-reader").apply { isDaemon = true }.start()
    }

    /** 最後に送った未完了の要求。なければ `null`。 */
    fun lastOutstanding(): CompletableFuture<Unit>? = synchronized(lock) { outstanding.lastOrNull() }

    /** [request] を送り、その読み上げの完了を表す [CompletableFuture] を返す。送れなければ完了済みを返す。 */
    fun send(request: String): CompletableFuture<Unit> =
        synchronized(lock) {
            val future = CompletableFuture<Unit>()
            if (closed) {
                future.complete(Unit)
                return future
            }
            outstanding.addLast(future)
            writeLine(request)
            future
        }

    /** 読み上げ中なら打ち切る。 */
    fun stop() {
        synchronized(lock) {
            if (outstanding.isNotEmpty()) writeLine(SPEAK_REQUEST_STOP)
        }
    }

    /**
     * [target] が現在の読み上げ（先頭の未完了要求）かつ最後に送った要求であれば打ち切る。
     * 後続の要求が送られていれば、その送信時にすでに打ち切り済みなので、
     * 古い呼び出しの打ち切りで新しい読み上げを止めない。
     */
    fun stopIfCurrent(target: CompletableFuture<Unit>) {
        synchronized(lock) {
            val isOnlyOutstanding = outstanding.firstOrNull() === target && outstanding.lastOrNull() === target
            if (isOnlyOutstanding) writeLine(SPEAK_REQUEST_STOP)
        }
    }

    private fun writeLine(line: String) {
        try {
            process.outputStream.write((line + "\n").toByteArray(Charsets.US_ASCII))
            process.outputStream.flush()
        } catch (e: IOException) {
            Sentry.captureException(e)
            close()
        }
    }

    private fun readResponses() {
        try {
            process.inputStream.bufferedReader(Charsets.UTF_8).forEachLine { line ->
                if (line == SPEAK_RESPONSE_DONE) {
                    synchronized(lock) { outstanding.removeFirstOrNull()?.complete(Unit) }
                }
            }
        } catch (_: IOException) {
            // プロセスの破棄による読み取り中断。後始末は下のcloseで行う。
        } finally {
            close()
        }
    }

    private fun close() {
        synchronized(lock) {
            closed = true
            outstanding.forEach { it.complete(Unit) }
            outstanding.clear()
        }
        process.destroy()
    }
}
