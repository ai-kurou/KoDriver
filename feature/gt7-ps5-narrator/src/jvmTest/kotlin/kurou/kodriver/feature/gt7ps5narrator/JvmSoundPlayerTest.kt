@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertFalse

class JvmSoundPlayerTest {

    private val player = JvmSoundPlayer()

    @Test
    fun `初期状態では isPlaying が false を返す`() {
        assertFalse(player.isPlaying)
    }

    @Test
    fun `不正なバイト列を渡しても例外が発生しない`() = runTest {
        player.play(ByteArray(0))
    }

    @Test
    fun `AudioSystem が利用できない環境で play しても isPlaying は false のまま`() = runTest {
        player.play(ByteArray(0))
        assertFalse(player.isPlaying)
    }

    @Test
    fun `音量0を指定しても例外が発生しない`() = runTest {
        player.play(ByteArray(0), volume = 0)
    }
}
