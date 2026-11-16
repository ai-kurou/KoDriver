package kurou.kodriver.core.narrator

/**
 * WAV 音声を再生するプラットフォーム実装の共通インターフェース。
 */
public interface SoundPlayer {
    public val isPlaying: Boolean

    public suspend fun play(
        bytes: ByteArray,
        volume: Int = 100,
    )
}
