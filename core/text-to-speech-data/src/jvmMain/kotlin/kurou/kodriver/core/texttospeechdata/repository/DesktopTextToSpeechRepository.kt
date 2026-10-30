package kurou.kodriver.core.texttospeechdata.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kurou.kodriver.core.texttospeechdata.windows.SapiSpeechSynthesizer
import kurou.kodriver.core.texttospeechdata.windows.SpeechSynthesizer
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.model.VOICE_PITCH_MAX
import kurou.kodriver.domain.model.VOICE_PITCH_MIN
import kurou.kodriver.domain.model.VOICE_SPEED_MAX
import kurou.kodriver.domain.model.VOICE_SPEED_MIN
import kurou.kodriver.domain.repository.TextToSpeechRepository

/**
 * OS標準の音声合成（Windows: SAPI / macOS: `say`）でテキストを読み上げる [TextToSpeechRepository]。
 *
 * 実際の音声合成の呼び出しは [SpeechSynthesizer] に切り出しており、ここでは
 * 読み上げ不要なテキストの除外とスレッド（[Dispatchers.IO]）の切り替えのみを担う。
 *
 * 利用できると判明した時点で [SpeechSynthesizer.warmUp] を呼び、最初の読み上げが遅れないようにする。
 *
 * [speak] は [SpeechSynthesizer.speak] が読み上げ完了までブロックする実装（[SapiSpeechSynthesizer]）
 * であることを前提に、その呼び出しを [runInterruptible] で包む。これにより、
 * 呼び出し元のコルーチンがキャンセルされるとブロック中のスレッドへ割り込みが送られ、
 * [SpeechSynthesizer.speak] 側でプロセスを破棄して読み上げを実際に打ち切ることができる。
 *
 * [SpeechSynthesizer.isAvailable] は外部プロセスを起動する重い判定のため、利用できると判明した後は
 * 結果を保持して再判定しない。利用できない間は、ユーザーが音声を導入した場合に検出できるよう
 * 呼び出しごとに再判定し、同時に複数のプロセスが起動しないよう排他する。
 */
internal class DesktopTextToSpeechRepository(
    private val synthesizer: SpeechSynthesizer = SapiSpeechSynthesizer(),
    private val isWindows: Boolean = System.getProperty("os.name").lowercase().startsWith("windows"),
) : TextToSpeechRepository {
    private val availabilityMutex = Mutex()
    private var availableConfirmed = false

    override suspend fun isAvailable(): Boolean =
        availabilityMutex.withLock {
            if (!availableConfirmed) {
                availableConfirmed =
                    withContext(Dispatchers.IO) {
                        synthesizer.isAvailable().also { if (it) synthesizer.warmUp() }
                    }
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
        voiceId: String,
        speed: Float,
        pitch: Float,
        audioUsage: AndroidAudioUsage?,
    ) {
        if (text.isBlank()) return
        runInterruptible(Dispatchers.IO) {
            synthesizer.speak(
                text,
                queue,
                volume.coerceIn(0, 100),
                voiceId,
                speed.coerceIn(VOICE_SPEED_MIN, VOICE_SPEED_MAX),
                pitch.coerceIn(VOICE_PITCH_MIN, VOICE_PITCH_MAX),
            )
        }
    }
}
