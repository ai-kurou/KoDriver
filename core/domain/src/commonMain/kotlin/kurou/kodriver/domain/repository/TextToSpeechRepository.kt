package kurou.kodriver.domain.repository

import kurou.kodriver.domain.model.TextToSpeechUnavailableReason

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
     * TTSが利用できない理由。利用できる場合は `null`。
     *
     * ユーザー向け案内に使う。Androidではエンジンと言語データを区別し、Windowsでは
     * 音声合成または日本語音声が使えない場合に [TextToSpeechUnavailableReason.WindowsSpeechUnavailable]
     * を返す。
     * [isAvailable] と同様、エンジンの初期化を伴う場合がある。
     */
    suspend fun unavailableReason(): TextToSpeechUnavailableReason?

    /**
     * [text] を読み上げる。実装は読み上げが実際に完了する（または [stop] やコルーチンのキャンセルで
     * 打ち切られる）まで suspend すること。`:core:narrator` の `WavNarratorEngine` はWAV再生と同じ
     * コルーチン上で完了・優先度判定・割り込みを扱う前提でこのRepositoryを呼び出すため、
     * 即座に返ってしまうと読み上げ中に次のイベントの音声が重なって再生されてしまう。
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
