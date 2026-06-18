package kurou.kodriver.domain.engine

interface TextToSpeechEngine {
    /** 現在再生中のイベントが属する ReadoutItemKey。再生していない場合は null。 */
    val currentReadoutItemKey: String?
    fun speak(event: SpeechEvent, queue: Boolean = false)
    fun stop()
}
