package kurou.kodriver.feature.narrator

interface SoundPlayer {
    val isPlaying: Boolean
    suspend fun play(bytes: ByteArray)
}
