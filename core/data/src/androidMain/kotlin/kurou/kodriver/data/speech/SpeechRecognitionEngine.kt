package kurou.kodriver.data.speech

/**
 * 1回分の聞き取りセッションを行う音声認識エンジン。
 * Android の `SpeechRecognizer` はメインスレッドでしか扱えず、結果通知の後はセッションが終わるため、
 * 常時聞き取りの再開は [AndroidSpeechRecognitionRepository] が担う。
 */
internal interface SpeechRecognitionEngine {
    /** 聞き取りを開始し、以降の通知を [listener] に渡す。 */
    fun start(listener: SpeechRecognitionEngineListener)

    /** 聞き取りを止めて資源を解放する。開始前や解放済みでも呼べる。 */
    fun stop()
}

internal interface SpeechRecognitionEngineListener {
    fun onReady()

    fun onPartial(text: String)

    /** [candidates] は信頼度の高い順。通知後にセッションは終了する。 */
    fun onResults(candidates: List<String>)

    /** [errorCode] は `SpeechRecognizer.ERROR_*`。通知後にセッションは終了する。 */
    fun onError(errorCode: Int)
}
