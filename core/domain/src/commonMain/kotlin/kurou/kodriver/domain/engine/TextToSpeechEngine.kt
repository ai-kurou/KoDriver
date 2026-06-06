package kurou.kodriver.domain.engine

interface TextToSpeechEngine {
    fun speak(text: String)
    fun stop()
}
