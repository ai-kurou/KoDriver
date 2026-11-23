package kurou.kodriver.domain.model

/**
 * マイクが聞き取った文言の、オーバーレイなど他画面へ見せるための表示用の状態。
 * 音声認識を行わない間（停止中・失敗後）は、すべて空の状態になる。
 */
public data class SpeechRecognitionTranscript(
    /** 認識途中の文字列。確定前のため、後続の結果で変わる。なければ空。 */
    val partialText: String = "",
    /** 直近に確定した発話の先頭候補。次の発話が確定するか、聞き取りを止めるまで残す。なければ null。 */
    val recognizedText: String? = null,
)
