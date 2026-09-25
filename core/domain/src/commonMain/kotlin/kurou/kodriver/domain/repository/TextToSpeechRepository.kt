package kurou.kodriver.domain.repository

/**
 * OS標準の音声合成（TTS）で任意のテキストを読み上げるRepository。
 *
 * KoDriver本来の読み上げは収録済みWAVの再生（`:core:narrator`）だが、ラップタイムのように
 * 事前収録では表現しきれない動的な文言を読み上げるための代替手段としてOSのTTSを使う。
 * 実装はWindows（デスクトップ）とAndroidのみで、それ以外のプラットフォームでは
 * [isAvailable] が `false` を返すNo-Op実装にフォールバックする。
 */
interface TextToSpeechRepository {
    /** このプラットフォームでTTSが利用できるかどうか。エンジンの初期化を伴う場合がある。 */
    suspend fun isAvailable(): Boolean

    /**
     * [text] を読み上げる。
     *
     * @param queue `true` なら再生中の読み上げの後ろへ追加し、`false` なら再生中の読み上げを
     *   打ち切って即座に読み上げる（`:core:narrator` の `speak(queue)` と同じ意味）。
     */
    suspend fun speak(
        text: String,
        queue: Boolean = false,
    )

    /** 再生中・キュー待ちの読み上げをすべて停止する。 */
    suspend fun stop()
}
