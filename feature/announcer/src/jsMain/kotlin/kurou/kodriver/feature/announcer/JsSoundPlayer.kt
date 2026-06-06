package kurou.kodriver.feature.announcer

class JsSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false
    override fun play(bytes: ByteArray) = Unit
}
