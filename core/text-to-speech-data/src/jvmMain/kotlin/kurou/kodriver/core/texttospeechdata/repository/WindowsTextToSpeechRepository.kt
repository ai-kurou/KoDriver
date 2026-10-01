package kurou.kodriver.core.texttospeechdata.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kurou.kodriver.core.texttospeechdata.windows.SapiSpeechSynthesizer
import kurou.kodriver.core.texttospeechdata.windows.WindowsSpeechSynthesizer
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.repository.TextToSpeechRepository

/**
 * Windows標準の音声合成でテキストを読み上げる [TextToSpeechRepository]。
 *
 * 実際の音声合成の呼び出しは [WindowsSpeechSynthesizer] に切り出しており、ここでは
 * 読み上げ不要なテキストの除外とスレッド（[Dispatchers.IO]）の切り替えのみを担う。
 *
 * [speak] は [WindowsSpeechSynthesizer.speak] が読み上げ完了までブロックする実装（[SapiSpeechSynthesizer]）
 * であることを前提に、その呼び出しを [runInterruptible] で包む。これにより、
 * 呼び出し元のコルーチンがキャンセルされるとブロック中のスレッドへ割り込みが送られ、
 * [WindowsSpeechSynthesizer.speak] 側でプロセスを破棄して読み上げを実際に打ち切ることができる。
 *
 * [WindowsSpeechSynthesizer.isAvailable] は外部プロセスを起動する重い判定のため、利用できると判明した後は
 * 結果を保持して再判定しない。利用できない間は、ユーザーが音声を導入した場合に検出できるよう
 * 呼び出しごとに再判定し、同時に複数のプロセスが起動しないよう排他する。
 */
internal class WindowsTextToSpeechRepository(
    private val synthesizer: WindowsSpeechSynthesizer = SapiSpeechSynthesizer(),
    private val isWindows: Boolean = System.getProperty("os.name").lowercase().startsWith("windows"),
) : TextToSpeechRepository {
    private val availabilityMutex = Mutex()
    private var availableConfirmed = false

    override suspend fun isAvailable(): Boolean =
        availabilityMutex.withLock {
            if (!availableConfirmed) {
                availableConfirmed = withContext(Dispatchers.IO) { synthesizer.isAvailable() }
            }
            availableConfirmed
        }

    override suspend fun unavailableReason(): TextToSpeechUnavailableReason? =
        if (!isWindows || isAvailable()) {
            null
        } else {
            TextToSpeechUnavailableReason.WindowsSpeechUnavailable
        }

    override suspend fun speak(
        text: String,
        queue: Boolean,
        volume: Int,
    ) {
        if (text.isBlank()) return
        runInterruptible(Dispatchers.IO) { synthesizer.speak(text, queue, volume.coerceIn(0, 100)) }
    }

    override suspend fun stop() {
        withContext(Dispatchers.IO) { synthesizer.stop() }
    }
}
