package kurou.kodriver.feature.main

/** マイク入力の確認用カードに表示する状態。 */
internal enum class MicrophoneInputStatus {
    /** 取得を止めている。 */
    Stopped,

    /** 取得を開始し、最初の音声を待っている。 */
    Starting,

    /** 音声を取得できている。 */
    Capturing,

    /** 入力デバイスが見つからない、または使えなくなった。 */
    Unavailable,

    /** 一定時間、完全な無音が続いた。OS のプライバシー設定でマイクがブロックされている可能性がある。 */
    NoSignal,
}

/** マイク入力の確認用カードの表示状態。 */
internal data class MicrophoneInputUiState(
    val enabled: Boolean = false,
    val status: MicrophoneInputStatus = MicrophoneInputStatus.Stopped,
    /** 選択中の入力デバイスの表示名。一覧の取得前や、デバイスが見つからない場合は null。 */
    val deviceName: String? = null,
    /** ドロップダウンに出す入力デバイス名の一覧。取得前や、デバイスが無い場合は空。 */
    val devices: List<String> = emptyList(),
    /** 直近の音量。0.0（無音）〜1.0（最大振幅）。 */
    val level: Float = 0f,
)
