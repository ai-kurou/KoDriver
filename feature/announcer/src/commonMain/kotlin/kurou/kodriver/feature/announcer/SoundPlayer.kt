package kurou.kodriver.feature.announcer

internal interface SoundPlayer {
    val isPlaying: Boolean
    fun play(bytes: ByteArray)
}
