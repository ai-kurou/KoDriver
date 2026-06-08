package kurou.kodriver.feature.narrator

class WasmJsSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false
    override suspend fun play(bytes: ByteArray) = Unit
}
