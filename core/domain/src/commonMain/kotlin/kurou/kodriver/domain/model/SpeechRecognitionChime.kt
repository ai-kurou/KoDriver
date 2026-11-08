package kurou.kodriver.domain.model

/** 音声認識が鳴らす確認音の種類。読み上げの開始音（[ReadoutStartSoundType]）とは別で、設定では変えられない。 */
enum class SpeechRecognitionChime {
    /** ウェイクワードを検出し、コマンドを待ち始めた。短い上昇音。 */
    WakeWordDetected,

    /** コマンドが一致しないまま待ち時間が過ぎ、ウェイクワード待ちへ戻る。反応音と逆向きの短い下降音。 */
    CommandTimedOut,
}
