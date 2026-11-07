package kurou.kodriver.core.speechrecognitiondata.speech

import kurou.kodriver.domain.model.SpeechRecognitionMode

/**
 * PCM音声を文字列に変換するエンジン。OS・ライブラリ依存部分をここに閉じ込め、Repository側はフェイクで検証する。
 */
internal interface SpeechTranscriber : AutoCloseable {
    /** 16kHz・モノラル・16bit PCM の [pcm] を渡す。発話の途中経過や確定結果が得られたときだけ返す。 */
    fun accept(pcm: ByteArray): SpeechTranscript?

    /**
     * 認識の対象を切り替える。生成直後は [SpeechRecognitionMode.WakeWord]。
     * [SpeechRecognitionMode.WakeWord] はウェイクワードに語彙を絞り、[SpeechRecognitionMode.Command] は絞らない。
     * [SpeechRecognitionMode.Paused] の間は [accept] が常に null を返す。切り替え前の途中経過は引き継がない。
     */
    fun setMode(mode: SpeechRecognitionMode)
}

/** [SpeechTranscriber] が返す認識結果。 */
internal sealed interface SpeechTranscript {
    /** 認識途中の文字列。確定前のため、後続の結果で変わる。 */
    data class Partial(
        val text: String,
    ) : SpeechTranscript

    /** 確定した認識結果。[candidates] は信頼度の高い順で、空にはならない。 */
    data class Final(
        val candidates: List<String>,
    ) : SpeechTranscript
}

/** [SpeechTranscriber] の生成元。認識モデルの有無の確認と読み込みを担う。 */
internal interface SpeechTranscriberFactory {
    /** 認識モデルが配置されていて使えるか。 */
    fun isAvailable(): Boolean

    /** 認識モデルを読み込んで [SpeechTranscriber] を作る。読み込めなければ例外を投げる。 */
    fun create(): SpeechTranscriber
}
