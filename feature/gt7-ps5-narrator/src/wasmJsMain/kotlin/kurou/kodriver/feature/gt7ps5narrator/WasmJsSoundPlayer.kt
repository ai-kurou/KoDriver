package kurou.kodriver.feature.gt7ps5narrator

/**
 * WAV 音声を再生する WasmJs 向け実装。
 */
class WasmJsSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false

    override suspend fun play(
        bytes: ByteArray,
        volume: Int,
    ) = Unit
}
