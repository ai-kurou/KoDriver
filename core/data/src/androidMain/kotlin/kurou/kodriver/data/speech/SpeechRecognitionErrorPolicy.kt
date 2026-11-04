package kurou.kodriver.data.speech

import android.speech.SpeechRecognizer
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason

internal sealed interface SpeechRecognitionErrorAction {
    /** [delayMs] 待ってから聞き取りを再開する。 */
    data class Restart(
        val delayMs: Long,
    ) : SpeechRecognitionErrorAction

    /** 聞き取りを続けられない。 */
    data class Stop(
        val reason: SpeechRecognitionFailureReason,
    ) : SpeechRecognitionErrorAction
}

/** 無音・認識不能のように、すぐ次の発話を待てるエラーの再開待ち時間。 */
internal const val SPEECH_RECOGNITION_QUICK_RESTART_DELAY_MS = 300L

/** 通信・音声入力・サービス側の一時的な不調から立ち直るための再開待ち時間。 */
internal const val SPEECH_RECOGNITION_RETRY_DELAY_MS = 1_000L

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
            SpeechRecognitionErrorAction.Restart(SPEECH_RECOGNITION_RETRY_DELAY_MS)
        }
    }
