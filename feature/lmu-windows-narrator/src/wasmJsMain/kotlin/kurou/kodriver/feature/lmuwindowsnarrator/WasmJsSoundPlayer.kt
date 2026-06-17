package kurou.kodriver.feature.lmuwindowsnarrator

class WasmJsSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false
    override suspend fun play(bytes: ByteArray, volume: Int) = Unit
}
