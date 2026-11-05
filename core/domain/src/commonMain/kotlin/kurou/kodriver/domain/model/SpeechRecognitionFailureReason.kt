package kurou.kodriver.domain.model

/**
 * 常時聞き取りを続けられない理由。一時的な失敗は Repository が内部で再開するため、ここには含めない。
 */
enum class SpeechRecognitionFailureReason {
    /** マイクの権限が許可されていない。 */
    PermissionDenied,

    /** 音声認識を端末で利用できない、または認識言語（日本語）に対応していない。 */
    Unavailable,
}
