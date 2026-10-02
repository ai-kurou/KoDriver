package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.TextToSpeechVoice

/**
 * Windowsの音声合成エンジンへテキストを渡して読み上げさせる。
 *
 * 実装（[SapiSpeechSynthesizer]）はWindows専用のプロセス（PowerShell + System.Speech）を
 * 起動するため、テストではFakeへ差し替える。
 */
interface WindowsSpeechSynthesizer {
    /** 実行環境がWindowsで、音声合成を利用できるかどうか。 */
    fun isAvailable(): Boolean

    /** 有効なインストール済み音声を返す。非Windows・取得失敗・タイムアウト時は空の一覧を返す。 */
    fun listVoices(): List<TextToSpeechVoice>

    /**
     * [text] を読み上げる。[queue] が `true` なら再生中の読み上げの完了を待ってから読み上げ、
     * `false` なら再生中の読み上げを打ち切ってから読み上げる。
     * [volume] は0〜100の音量。
     */
    fun speak(
        text: String,
        queue: Boolean,
        volume: Int,
    )

    /** 再生中の読み上げを停止する。 */
    fun stop()
}
