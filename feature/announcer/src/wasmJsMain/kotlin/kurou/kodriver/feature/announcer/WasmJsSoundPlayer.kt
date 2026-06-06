package kurou.kodriver.feature.announcer

internal class WasmJsSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false
    override fun play(bytes: ByteArray) = Unit
}
