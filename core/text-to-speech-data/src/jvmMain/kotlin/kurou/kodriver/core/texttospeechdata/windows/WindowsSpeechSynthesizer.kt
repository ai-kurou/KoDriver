package kurou.kodriver.core.texttospeechdata.windows

/**
 * Windowsの音声合成エンジンへテキストを渡して読み上げさせる。
 *
 * 実装（[SapiSpeechSynthesizer]）はWindows専用のプロセス（PowerShell + System.Speech）を
 * 起動するため、テストではFakeへ差し替える。
 */
interface WindowsSpeechSynthesizer {
    /** 実行環境がWindowsで、音声合成を利用できるかどうか。 */
    fun isAvailable(): Boolean

    /**
     * [text] を読み上げる。[queue] が `true` なら再生中の読み上げの完了を待ってから読み上げ、
     * `false` なら再生中の読み上げを打ち切ってから読み上げる。
     */
    fun speak(
        text: String,
        queue: Boolean,
    )

    /** 再生中の読み上げを停止する。 */
    fun stop()
}
