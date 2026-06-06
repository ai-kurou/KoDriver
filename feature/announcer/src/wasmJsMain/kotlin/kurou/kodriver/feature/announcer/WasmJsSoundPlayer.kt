package kurou.kodriver.feature.announcer

class WasmJsSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false
    override fun play(bytes: ByteArray) = Unit
}
