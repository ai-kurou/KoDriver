package kurou.kodriver.feature.announcer

internal interface SoundPlayer {
    fun play(bytes: ByteArray)
}
