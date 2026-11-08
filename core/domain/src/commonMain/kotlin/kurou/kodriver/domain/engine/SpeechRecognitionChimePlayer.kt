package kurou.kodriver.domain.engine

/**
 * 音声認識がウェイクワードを検出したことを知らせる確認音を鳴らす。
 *
 * 読み上げの開始音（[kurou.kodriver.domain.model.ReadoutStartSoundType]）とは別の音で、設定では変えられない。
 */
interface SpeechRecognitionChimePlayer {
    /** 確認音を鳴らし、鳴り終わるまで待つ。 */
    suspend fun play()
}
