package kurou.kodriver.feature.narrator

class JsSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false
    override fun play(bytes: ByteArray) = Unit
}
