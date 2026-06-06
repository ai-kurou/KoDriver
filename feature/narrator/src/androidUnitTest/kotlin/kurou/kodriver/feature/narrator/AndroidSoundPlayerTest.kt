package kurou.kodriver.feature.narrator

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertFalse

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidSoundPlayerTest {

    private lateinit var player: AndroidSoundPlayer

    @Before
    fun setUp() {
        player = AndroidSoundPlayer(RuntimeEnvironment.getApplication())
    }

    @Test
    fun `初期状態では isPlaying が false を返す`() {
        assertFalse(player.isPlaying)
    }

    @Test
    fun `不正なバイト列を渡しても例外が発生しない`() {
        player.play(ByteArray(0))
    }

    @Test
    fun `play 後も isPlaying は false のまま（prepareAsync 完了前）`() {
        player.play(ByteArray(0))
        assertFalse(player.isPlaying)
    }
}
