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
 * `volume`と`pitch`はmacでは非対応のため無視する。`speed`は標準175wpmに掛けて`-r`へ渡す。
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

    /** [volume] と [pitch] はmacの`say`では指定できないため無視する。 */
    override fun speak(
        text: String,
        queue: Boolean,
        volume: Int,
        voiceId: String,
        speed: Float,
        pitch: Float,
    ) {
        if (!isMac) return
        val token = synchronized(lock) { ++requestToken }
        val command =
            listOf(
                SAY,
                "-v",
                voiceId.ifEmpty { DEFAULT_VOICE },
                "-r",
                (BASE_WORDS_PER_MINUTE * speed).roundToInt().toString(),
                "--",
                text,
            )
        val process = launch(token, command, queue) ?: return
        try {
            process.waitFor()
        } catch (e: InterruptedException) {
            process.destroy()
            throw e
        } finally {
            synchronized(lock) { if (current === process) current = null }
        }
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
                    if (!queue) playing?.destroy()
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
        const val DEFAULT_VOICE = "Kyoko"
        const val BASE_WORDS_PER_MINUTE = 175
        const val LIST_VOICES_TIMEOUT_SECONDS = 15L
        const val DESTROY_WAIT_TIMEOUT_SECONDS = 5L
    }
}
