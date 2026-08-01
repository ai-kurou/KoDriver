package kurou.kodriver.feature.lmuwindowsnarrator

internal interface SoundPlayer {
    val isPlaying: Boolean

    suspend fun play(bytes: ByteArray, volume: Int = 100)
}
