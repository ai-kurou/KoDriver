package kurou.kodriver.domain.model

/**
 * マイク入力の状態。入力デバイスの確認表示に使う。
 */
sealed interface MicrophoneInputStatus {
    /** 取得を開始し、最初の音声を待っている。 */
    data object Starting : MicrophoneInputStatus

    /**
     * 音声を取得できている。
     * [level] は直近の断片の音量（RMS）を 0.0（無音）〜1.0（最大振幅）に正規化した値。
     */
    data class Capturing(
        val level: Float,
    ) : MicrophoneInputStatus

    /** 取得を続けられない。通知後にストリームは完了する。 */
    data class Failed(
        val reason: MicrophoneFailureReason,
    ) : MicrophoneInputStatus
}
