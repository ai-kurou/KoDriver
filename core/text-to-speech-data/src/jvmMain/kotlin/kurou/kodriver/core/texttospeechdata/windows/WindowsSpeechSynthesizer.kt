package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.TextToSpeechVoice

/**
 * Windowsの音声合成エンジンへテキストを渡して読み上げさせる。
 *
 * 実装（[SapiSpeechSynthesizer]）はWindows専用のプロセス（PowerShell + System.Speech）を
 * 常駐させるため、テストではFakeへ差し替える。
 */
interface WindowsSpeechSynthesizer {
    /** 実行環境がWindowsで、音声合成を利用できるかどうか。 */
    fun isAvailable(): Boolean

    /** 有効なインストール済み音声を返す。非Windows・取得失敗・タイムアウト時は空の一覧を返す。 */
    fun listVoices(): List<TextToSpeechVoice>

    /** 最初の読み上げが遅れないよう、読み上げの準備を先に済ませる。準備を要しない実装では何もしない。 */
    fun warmUp() {}

    /**
     * [text] を読み上げる。[queue] が `true` なら再生中の読み上げの完了を待ってから読み上げ、
     * `false` なら再生中の読み上げを打ち切ってから読み上げる。
     * [volume] は0〜100の音量。[voiceId] が空ならシステム既定の日本語音声を使い、
     * 指定音声が見つからない場合も日本語音声へフォールバックする。
     * [pitch] は1.0が標準の声の高さ（0.5〜2.0）。SSMLのprosody pitchで反映する。
     * 使用中の音声がpitchに対応しない場合は変化しない場合がある。
     * [speed] は1.0が標準の読み上げ速度（0.5〜2.0）。
     */
    fun speak(
        text: String,
        queue: Boolean,
        volume: Int,
        voiceId: String,
        speed: Float,
        pitch: Float,
    )

    /** 再生中の読み上げを停止する。 */
    fun stop()
}
