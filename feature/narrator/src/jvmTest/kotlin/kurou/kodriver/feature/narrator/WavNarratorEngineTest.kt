package kurou.kodriver.feature.narrator

import org.junit.Test
import kotlin.test.assertEquals

class WavNarratorEngineTest {

    private val fakePlayer = FakeSoundPlayer()
    private val engine = WavNarratorEngine(fakePlayer)

    @Test
    fun `isPlaying が true のとき speak しても play が呼ばれない`() {
        fakePlayer.isPlayingValue = true
        engine.speak("カーレフト")
        assertEquals(0, fakePlayer.playCallCount)
    }

    @Test
    fun `isPlaying が false で未登録テキストを speak しても play が呼ばれない`() {
        fakePlayer.isPlayingValue = false
        engine.speak("未登録テキスト")
        assertEquals(0, fakePlayer.playCallCount)
    }

    @Test
    fun `stop を呼んでも例外が発生しない`() {
        engine.stop()
    }

    @Test
    fun `isPlaying が false で登録済みテキストを speak しても sounds 未ロード時は play が呼ばれない`() {
        fakePlayer.isPlayingValue = false
        // テスト環境では Res.readBytes() が失敗するため sounds は空
        engine.speak("カーレフト")
        assertEquals(0, fakePlayer.playCallCount)
    }
}

private class FakeSoundPlayer : SoundPlayer {
    var isPlayingValue: Boolean = false
    override val isPlaying: Boolean get() = isPlayingValue

    var playCallCount = 0
    override suspend fun play(bytes: ByteArray) {
        playCallCount++
    }
}
