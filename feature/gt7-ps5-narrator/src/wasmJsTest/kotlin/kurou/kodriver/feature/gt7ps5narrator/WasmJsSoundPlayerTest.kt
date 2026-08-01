@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse

class WasmJsSoundPlayerTest {

    private val player = WasmJsSoundPlayer()

    @Test
    fun `isPlaying は常に false を返す`() {
        assertFalse(player.isPlaying)
    }

    @Test
    fun `play を呼んでも例外が発生しない`() =
        runTest {
        player.play(ByteArray(0))
    }

    @Test
    fun `音量0を指定しても例外が発生しない`() =
        runTest {
        player.play(ByteArray(0), volume = 0)
    }
}
