package kurou.kodriver.domain.repository

import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED

/**
 * OS標準の音声合成（TTS）で任意のテキストを読み上げるRepository。
 *
 * LMUフラッグの自由文字列や、ラップタイムなどの動的な文言をOSのTTSで読み上げる。
 * 開始音のWAV再生は `:core:narrator` が担当する。
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
     * 打ち切られる）まで suspend すること。`:core:narrator` の `NarratorEngine` は開始音と同じ
     * コルーチン上で完了・優先度判定・割り込みを扱う前提でこのRepositoryを呼び出すため、
     * 即座に返ってしまうと読み上げ中に次のイベントの音声が重なって再生されてしまう。
     *
     * @param queue `true` なら再生中の読み上げの後ろへ追加し、`false` なら再生中の読み上げを
     *   打ち切って即座に読み上げる（`:core:narrator` の `speak(queue)` と同じ意味）。
     * @param voiceId Windowsは音声名、Androidは Voice.name。空ならシステム既定、
     *   見つからなければ日本語音声へフォールバックする。
     * @param volume 読み上げ音量（0〜100）。アプリの読み上げ音量設定に合わせる。0なら発話しない。
     */
    suspend fun speak(
        text: String,
        queue: Boolean = false,
        volume: Int = 100,
        voiceId: String = VOICE_ID_UNSPECIFIED,
    )

    /** 再生中・キュー待ちの読み上げをすべて停止する。 */
    suspend fun stop()
}
