package kurou.kodriver.domain.engine

interface TextToSpeechEngine {
    fun speak(event: SpeechEvent)
    fun stop()
}
