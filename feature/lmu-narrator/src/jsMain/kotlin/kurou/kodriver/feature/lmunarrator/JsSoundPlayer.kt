package kurou.kodriver.feature.lmunarrator

class JsSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false
    override suspend fun play(bytes: ByteArray) = Unit
}
