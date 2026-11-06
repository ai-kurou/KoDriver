package kurou.kodriver.feature.main

/** 認識結果の履歴として残す件数。確認用の簡易表示のため少なめにしている。 */
internal const val SPEECH_RECOGNITION_HISTORY_MAX = 5

/** 音声認識の聞き取り状態。 */
internal enum class SpeechRecognitionStatus {
    /** 聞き取りを止めている。 */
    Stopped,

    /** 聞き取りを開始し、認識の準備が整うのを待っている。 */
    Starting,

    /** 発話を待っている。 */
    Listening,

    /** マイクの権限が許可されず、聞き取りを開始できない。 */
    PermissionDenied,

    /** 音声認識を端末で利用できない。 */
    Unavailable,

    /** マイクを開けない、または取得中に使えなくなった。 */
    MicrophoneUnavailable,

    /** 完全な無音が続いた。OSのプライバシー設定でマイクがブロックされている可能性がある。 */
    NoSignal,

    /** 認識中に想定外のエラーが起きた。 */
    RecognitionFailed,

    /** しばらく発話がなかったため、聞き取りを自動で止めた。 */
    IdleStopped,
}

/** 音声認識の確認用カードの表示状態。 */
internal data class SpeechRecognitionUiState(
    val enabled: Boolean = false,
    val status: SpeechRecognitionStatus = SpeechRecognitionStatus.Stopped,
    val partialText: String = "",
    /** 直近の入力音量。0.0（無音）〜1.0（大音量）。音量を通知しないプラットフォームや、聞き取りを止めている間は null。 */
    val level: Float? = null,
    /** 確定した認識結果の履歴。新しい順で、各要素は信頼度の高い順の候補。 */
    val recognized: List<List<String>> = emptyList(),
    /** ドロップダウンに出す入力デバイス名の一覧。デバイスを選べないプラットフォーム（Android）や、取得前、デバイスが無い場合は空。 */
    val devices: List<String> = emptyList(),
    /** 選択中の入力デバイス名。一覧の取得前や、デバイスが無い場合は null。 */
    val deviceName: String? = null,
)
