package kurou.kodriver.domain.model

/**
 * 常時聞き取り中に音声認識から通知されるイベント。
 */
sealed interface SpeechRecognitionEvent {
    /** 聞き取りの準備が整い、発話を待っている。再開のたびに通知する。 */
    data object Listening : SpeechRecognitionEvent

    /**
     * 聞き取り中の入力音量。[level] は 0.0（無音）〜1.0（大音量）に正規化した値。
     * Android は認識エンジンの通知、デスクトップは取得した音声の断片ごとの音量を流す。
     */
    data class Level(
        val level: Float,
    ) : SpeechRecognitionEvent

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
