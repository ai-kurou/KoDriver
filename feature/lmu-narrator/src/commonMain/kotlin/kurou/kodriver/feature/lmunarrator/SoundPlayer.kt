package kurou.kodriver.feature.lmunarrator

interface SoundPlayer {
    val isPlaying: Boolean
    suspend fun play(bytes: ByteArray)
}
