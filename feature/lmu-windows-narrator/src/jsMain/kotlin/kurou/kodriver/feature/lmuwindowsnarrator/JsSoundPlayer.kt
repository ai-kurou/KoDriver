package kurou.kodriver.feature.lmuwindowsnarrator

/**
 * WAV 音声を再生する Js 向け実装。
 */
class JsSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false

    override suspend fun play(bytes: ByteArray, volume: Int) = Unit
}
