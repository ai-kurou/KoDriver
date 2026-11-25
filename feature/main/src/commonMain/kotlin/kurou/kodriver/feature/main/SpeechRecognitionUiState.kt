package kurou.kodriver.feature.main

/** 音声認識の聞き取り状態。 */
internal enum class SpeechRecognitionStatus {
    /** 聞き取りを止めている。 */
    Stopped,

    /** 聞き取りを開始し、認識の準備が整うのを待っている。 */
    Starting,

    /** ウェイクワードを待っている。 */
    Listening,

    /** ウェイクワードを検出し、読み上げ項目のコマンドを待っている。時間切れでウェイクワード待ちへ戻る。 */
    CommandWaiting,

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
}

/** 音声認識の確認用カードの表示状態。 */
internal data class SpeechRecognitionUiState(
    val enabled: Boolean = false,
    val status: SpeechRecognitionStatus = SpeechRecognitionStatus.Stopped,
    val partialText: String = "",
    /** 直近の入力音量。0.0（無音）〜1.0（大音量）。音量を通知しないプラットフォームや、聞き取りを止めている間は null。 */
    val level: Float? = null,
    /** 「小さい音をカット」の閾値。0.0〜1.0 で [level] と同じ尺度。この値未満の音は認識に使われない。0f はカットしない。 */
    val cutThreshold: Float = 0f,
    /** 「コマンド受付中もカットする」の設定。オフのとき、コマンド待ちの間は [cutThreshold] を使わない。 */
    val cutInCommand: Boolean = false,
    /** 確定した認識結果の履歴。新しい順で、各要素は信頼度の高い順の候補。 */
    val recognized: List<List<String>> = emptyList(),
    /** ドロップダウンに出す入力デバイス名の一覧。デバイスを選べないプラットフォーム（Android）や、取得前、デバイスが無い場合は空。 */
    val devices: List<String> = emptyList(),
    /** 選択中の入力デバイス名。一覧の取得前や、デバイスが無い場合は null。 */
    val deviceName: String? = null,
) {
    /**
     * 今の聞き取りで実際に使われるカットの閾値。コマンド待ち中は、[cutInCommand] がオフならカットしないので 0f。
     * ゲージやリングの表示には [cutThreshold] ではなくこちらを使う。
     */
    val effectiveCutThreshold: Float
        get() = if (status == SpeechRecognitionStatus.CommandWaiting && !cutInCommand) 0f else cutThreshold
}
