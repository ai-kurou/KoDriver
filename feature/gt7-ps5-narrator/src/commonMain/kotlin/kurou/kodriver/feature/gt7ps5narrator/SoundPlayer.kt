package kurou.kodriver.feature.gt7ps5narrator

interface SoundPlayer {
    val isPlaying: Boolean
    suspend fun play(bytes: ByteArray, volume: Int = 100)
}
