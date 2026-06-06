package kurou.kodriver.feature.narrator

interface SoundPlayer {
    val isPlaying: Boolean
    fun play(bytes: ByteArray)
}
