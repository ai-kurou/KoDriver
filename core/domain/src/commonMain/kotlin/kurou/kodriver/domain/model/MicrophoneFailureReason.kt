package kurou.kodriver.domain.model

/**
 * マイクからの取得を続けられない理由。
 */
enum class MicrophoneFailureReason {
    /** 入力デバイスが見つからない、開けない、または取得中に使えなくなった。 */
    Unavailable,

    /**
     * 一定時間、完全な無音（全サンプルが0）が続いた。
     * OSのプライバシー設定でマイクがブロックされている場合などに起きる。
     */
    NoSignal,
}
