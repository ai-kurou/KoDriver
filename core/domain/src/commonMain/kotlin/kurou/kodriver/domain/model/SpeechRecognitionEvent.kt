package kurou.kodriver.domain.model

/**
 * 常時聞き取り中に音声認識から通知されるイベント。
 */
sealed interface SpeechRecognitionEvent {
    /** 聞き取りの準備が整い、発話を待っている。再開のたびに通知する。 */
    data object Listening : SpeechRecognitionEvent

    /** 認識途中の文字列。確定前のため、後続の結果で変わる。 */
    data class Partial(
        val text: String,
    ) : SpeechRecognitionEvent

    /** 確定した認識結果。[candidates] は信頼度の高い順で、空になることはない。 */
    data class Recognized(
        val candidates: List<String>,
    ) : SpeechRecognitionEvent

    /** 聞き取りを続けられない。通知後にストリームは完了する。 */
    data class Failed(
        val reason: SpeechRecognitionFailureReason,
    ) : SpeechRecognitionEvent
}
