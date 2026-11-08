package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.SpeechRecognitionChime

/**
 * 音声認識の状態変化を知らせる確認音（[SpeechRecognitionChime]）を鳴らす。
 *
 * 読み上げの開始音（[kurou.kodriver.domain.model.ReadoutStartSoundType]）とは別の音で、設定では変えられない。
 */
interface SpeechRecognitionChimePlayer {
    /** [chime] の確認音を鳴らし、鳴り終わるまで待つ。 */
    suspend fun play(chime: SpeechRecognitionChime)
}
