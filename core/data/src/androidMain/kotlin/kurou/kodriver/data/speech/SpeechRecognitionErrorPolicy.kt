package kurou.kodriver.data.speech

import android.speech.SpeechRecognizer
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason

internal sealed interface SpeechRecognitionErrorAction {
    /** 認識は正常に動いている。[delayMs] 待ってから聞き取りを再開し、連続失敗の回数は数え直す。 */
    data class Restart(
        val delayMs: Long,
    ) : SpeechRecognitionErrorAction

    /** 一時的な失敗。連続失敗として数え、回数に応じて待ち時間を延ばして再開する。 */
    data object Retry : SpeechRecognitionErrorAction

    /** 聞き取りを続けられない。 */
    data class Stop(
        val reason: SpeechRecognitionFailureReason,
    ) : SpeechRecognitionErrorAction
}

/** 無音・認識不能のように、すぐ次の発話を待てるエラーの再開待ち時間。 */
internal const val SPEECH_RECOGNITION_QUICK_RESTART_DELAY_MS = 300L

/** 通信・音声入力・サービス側の一時的な不調から立ち直るための、1回目の再開待ち時間。以降は倍々に延ばす。 */
internal const val SPEECH_RECOGNITION_RETRY_DELAY_MS = 1_000L

/** 再開待ち時間の上限。 */
internal const val SPEECH_RECOGNITION_RETRY_DELAY_MAX_MS = 30_000L

/** 連続して失敗できる回数。到達したら聞き取りを諦めて利用不可として完了する。 */
internal const val SPEECH_RECOGNITION_RETRY_LIMIT = 10

/** 待ち時間の計算でシフトする回数の上限。上限の待ち時間に十分達する大きさにしてある。 */
private const val MAX_BACKOFF_EXPONENT = 10

/** 連続失敗の回数に応じた再開待ち時間。1回目は [SPEECH_RECOGNITION_RETRY_DELAY_MS] で、倍々に延ばして上限で止める。 */
internal fun speechRecognitionRetryDelayMs(consecutiveFailures: Int): Long {
    val exponent = (consecutiveFailures - 1).coerceIn(0, MAX_BACKOFF_EXPONENT)
    return (SPEECH_RECOGNITION_RETRY_DELAY_MS shl exponent).coerceAtMost(SPEECH_RECOGNITION_RETRY_DELAY_MAX_MS)
}

internal fun speechRecognitionErrorAction(errorCode: Int): SpeechRecognitionErrorAction =
    when (errorCode) {
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
            SpeechRecognitionErrorAction.Stop(SpeechRecognitionFailureReason.PermissionDenied)
        }

        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
        -> {
            SpeechRecognitionErrorAction.Stop(SpeechRecognitionFailureReason.Unavailable)
        }

        SpeechRecognizer.ERROR_NO_MATCH,
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
        -> {
            SpeechRecognitionErrorAction.Restart(SPEECH_RECOGNITION_QUICK_RESTART_DELAY_MS)
        }

        else -> {
            SpeechRecognitionErrorAction.Retry
        }
    }
