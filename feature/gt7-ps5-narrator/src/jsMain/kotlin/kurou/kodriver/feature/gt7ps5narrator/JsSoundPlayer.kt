package kurou.kodriver.feature.gt7ps5narrator

class JsSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false
    override suspend fun play(bytes: ByteArray, volume: Int) = Unit
}
