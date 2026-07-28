package kurou.kodriver.feature.acewindowsnarrator

internal interface SoundPlayer {
    val isPlaying: Boolean
    suspend fun play(bytes: ByteArray, volume: Int = 100)
}
