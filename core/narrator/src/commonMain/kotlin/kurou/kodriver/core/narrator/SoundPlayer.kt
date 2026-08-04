package kurou.kodriver.core.narrator

/**
 * WAV 音声を再生するプラットフォーム実装の共通インターフェース。
 */
interface SoundPlayer {
    val isPlaying: Boolean

    suspend fun play(
        bytes: ByteArray,
        volume: Int = 100,
    )
}
