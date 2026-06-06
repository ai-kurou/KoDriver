package kurou.kodriver.feature.announcer

interface SoundPlayer {
    val isPlaying: Boolean
    fun play(bytes: ByteArray)
}
