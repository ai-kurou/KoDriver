package kurou.kodriver.core.speechrecognitiondata.speech

/**
 * 1回分の聞き取りセッションを行う音声認識エンジン。
 * Android の `SpeechRecognizer` はメインスレッドでしか扱えず、結果通知の後はセッションが終わるため、
 * 常時聞き取りの再開は [AndroidSpeechRecognitionRepository] が担う。
 */
internal interface SpeechRecognitionEngine {
    /** 端末内認識を使っているか。端末内認識が言語エラーになった場合に、通常の認識へ切り替える判断に使う。 */
    val isOnDevice: Boolean

    /** 聞き取りを開始し、以降の通知を [listener] に渡す。 */
    fun start(listener: SpeechRecognitionEngineListener)

    /** 聞き取りを止めて資源を解放する。開始前や解放済みでも呼べる。 */
    fun stop()
}

internal interface SpeechRecognitionEngineListener {
    fun onReady()

    fun onPartial(text: String)

    /** [level] は入力音量を 0.0（無音）〜1.0（大音量）に正規化した値。聞き取り中に頻繁に通知される。 */
    fun onLevel(level: Float)

    /** [candidates] は信頼度の高い順。通知後にセッションは終了する。 */
    fun onResults(candidates: List<String>)

    /** [errorCode] は `SpeechRecognizer.ERROR_*`。通知後にセッションは終了する。 */
    fun onError(errorCode: Int)
}
