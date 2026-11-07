package kurou.kodriver.domain.model

/**
 * 音声認識が今、何を聞き取るか。
 */
enum class SpeechRecognitionMode {
    /** ウェイクワードだけを待つ。語彙をウェイクワードに絞って認識する。 */
    WakeWord,

    /** ウェイクワードの後で、読み上げ項目のコマンドを待つ。語彙は絞らない。 */
    Command,

    /** 読み上げ中。自分の読み上げ音声を認識しないよう、聞き取りを止める。 */
    Paused,
}
