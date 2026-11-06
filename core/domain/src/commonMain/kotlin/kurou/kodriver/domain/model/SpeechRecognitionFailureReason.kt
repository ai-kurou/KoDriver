package kurou.kodriver.domain.model

/**
 * 常時聞き取りを続けられない理由。一時的な失敗は Repository が内部で再開するため、ここには含めない。
 */
enum class SpeechRecognitionFailureReason {
    /** マイクの権限が許可されていない。 */
    PermissionDenied,

    /** 音声認識を端末で利用できない、または認識言語（日本語）に対応していない。デスクトップでは認識モデルを読み込めない場合を含む。 */
    Unavailable,

    /** 入力デバイス（マイク）を開けない、または取得中に使えなくなった（デスクトップ）。 */
    MicrophoneUnavailable,

    /** 完全な無音が続いた。OSのプライバシー設定でマイクがブロックされている場合などに起きる（デスクトップ）。 */
    NoSignal,

    /** 認識中に想定外のエラーが起きた（デスクトップ）。詳細はエラーログに残る。 */
    RecognitionFailed,
}
