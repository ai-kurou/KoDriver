package kurou.kodriver.core.texttospeechdata.windows

import io.sentry.Sentry
import kurou.kodriver.domain.model.TextToSpeechVoice
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * macの`say`コマンドを使う [WindowsSpeechSynthesizer] の実装（macOSでの動作確認用）。
 *
 * 読み上げは`say -v <音声> -r <wpm> -- <テキスト>`を1発話ごとに起動し、プロセスの終了（読み上げ完了）までブロックする。
 * `volume`は0のとき発話しないことだけに使い、`pitch`はmacでは非対応のため無視する。`speed`は標準175wpmに掛けて`-r`へ渡す。
 * `queue = false` では再生中のプロセスを打ち切ってから開始し、`queue = true` では再生中のプロセスの終了を
 * [lock] の外で待ってから開始する（[SapiSpeechSynthesizer] と同様、待機中に別の呼び出しに追い越されたら再生しない）。
 * スレッド割り込み（コルーチンのキャンセル）時は自分のプロセスを終了して例外を再送出する。
 */
internal class MacSpeechSynthesizer(
    private val isMac: Boolean = System.getProperty("os.name").lowercase().startsWith("mac"),
    private val startProcess: (ProcessBuilder) -> Process = ProcessBuilder::start,
    private val captureException: (Throwable) -> Unit = Sentry::captureException,
) : WindowsSpeechSynthesizer {
    private val lock = Any()
    private var current: Process? = null
    private var requestToken = 0L

    override fun isAvailable(): Boolean = isMac && listVoices().isNotEmpty()

    override fun listVoices(): List<TextToSpeechVoice> {
        if (!isMac) return emptyList()
        return try {
            // 終了待ち中に標準出力のバッファが満杯になるのを避けるため、一時ファイルへ出力する。
            val output = Files.createTempFile("kodriver-voices-", ".txt")
            try {
                readVoices(output)
            } finally {
                Files.deleteIfExists(output)
            }
        } catch (e: Exception) {
            captureException(e)
            emptyList()
        }
    }

    private fun readVoices(output: Path): List<TextToSpeechVoice> {
        val process =
            startProcess(
                ProcessBuilder(SAY, "-v", "?")
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .redirectOutput(output.toFile()),
            )
        try {
            if (!process.waitFor(LIST_VOICES_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly().waitFor(DESTROY_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                error("音声一覧の取得がタイムアウトしました")
            }
            check(process.exitValue() == 0) { "音声一覧の取得に失敗しました（終了コード: ${process.exitValue()}）" }
            return parseMacVoiceList(Files.readString(output, Charsets.UTF_8))
        } finally {
            process.destroy()
        }
    }

    /** macの`say`では音量と声の高さを指定できないため、[volume] は0のとき発話しないことだけに使い、[pitch] は無視する。 */
    override fun speak(
        text: String,
        queue: Boolean,
        volume: Int,
        voiceId: String,
        speed: Float,
        pitch: Float,
    ) {
        if (!isMac || volume <= 0) return
        val token = synchronized(lock) { ++requestToken }
        // 指定音声が未導入などで失敗した場合は、日本語の既定音声で読み直す。
        val voices = listOf(voiceId.ifEmpty { MAC_DEFAULT_VOICE }, MAC_DEFAULT_VOICE).distinct()
        voices.forEachIndexed { index, voice ->
            val command = sayCommand(voice, speed, text)
            val process = launch(token, command, queue && index == 0) ?: return
            val exitCode = await(process)
            // 後続の呼び出しに打ち切られた場合は、失敗として扱わない。
            if (exitCode == 0 || !isLatest(token)) return
            if (index == voices.lastIndex) captureException(IllegalStateException("sayが失敗しました（終了コード: $exitCode）"))
        }
    }

    private fun sayCommand(
        voice: String,
        speed: Float,
        text: String,
    ) = listOf(SAY, "-v", voice, "-r", (BASE_WORDS_PER_MINUTE * speed).roundToInt().toString(), "--", text)

    private fun isLatest(token: Long) = synchronized(lock) { token == requestToken }

    /** [process] の終了を待って終了コードを返す。割り込まれたら [process] を終了して例外を再送出する。 */
    private fun await(process: Process): Int =
        try {
            process.waitFor()
        } catch (e: InterruptedException) {
            process.destroy()
            // 終了前に次の発話が始まって音声が重ならないよう、終了を待ってから current を解除する。
            process.waitFor(DESTROY_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            throw e
        } finally {
            synchronized(lock) { if (current === process) current = null }
        }

    /** 開始できたら起動したプロセスを返す。追い越された場合、または起動に失敗した場合は `null`。 */
    private fun launch(
        token: Long,
        command: List<String>,
        queue: Boolean,
    ): Process? {
        while (true) {
            val blocker =
                synchronized(lock) {
                    if (token != requestToken) return null
                    val playing = current
                    if (!queue && playing != null) {
                        playing.destroy()
                        // 打ち切った発話の音声が新しい発話と重ならないよう、終了を待つ。
                        playing.waitFor(DESTROY_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    }
                    if (queue && playing != null) {
                        playing
                    } else {
                        return try {
                            startProcess(ProcessBuilder(command)).also { current = it }
                        } catch (e: IOException) {
                            captureException(e)
                            null
                        }
                    }
                }
            blocker.waitFor()
        }
    }

    private companion object {
        const val SAY = "say"
        const val BASE_WORDS_PER_MINUTE = 175
        const val LIST_VOICES_TIMEOUT_SECONDS = 15L
        const val DESTROY_WAIT_TIMEOUT_SECONDS = 5L
    }
}
