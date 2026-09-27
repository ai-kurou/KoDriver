package kurou.kodriver

import kurou.kodriver.presentation.NarratorOverlayWindowBounds
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OverlayWindowBoundsScreenCheckTest {
    private val primaryScreen = Rectangle(0, 0, 1920, 1080)
    private val secondaryScreen = Rectangle(-1920, 0, 1920, 1080)

    @Test
    fun `位置が未設定なら復元しない`() {
        val bounds = NarratorOverlayWindowBounds(x = null, y = null, width = 480, height = 120)

        assertNull(bounds.restorablePosition(listOf(primaryScreen)))
    }

    @Test
    fun `画面内に収まっていれば復元する`() {
        val bounds = NarratorOverlayWindowBounds(x = 100, y = 100, width = 480, height = 120)

        assertNotNull(bounds.restorablePosition(listOf(primaryScreen)))
    }

    @Test
    fun `別モニタの座標でもそのモニタが接続されていれば復元する`() {
        val bounds = NarratorOverlayWindowBounds(x = -1800, y = 100, width = 480, height = 120)

        assertNotNull(bounds.restorablePosition(listOf(primaryScreen, secondaryScreen)))
    }

    @Test
    fun `保存時のモニタが外されていれば復元しない`() {
        val bounds = NarratorOverlayWindowBounds(x = -1800, y = 100, width = 480, height = 120)

        assertNull(bounds.restorablePosition(listOf(primaryScreen)))
    }

    @Test
    fun `画面から完全に外れていれば復元しない`() {
        val bounds = NarratorOverlayWindowBounds(x = 5000, y = 5000, width = 480, height = 120)

        assertNull(bounds.restorablePosition(listOf(primaryScreen)))
    }

    @Test
    fun `画面と重なる領域が小さすぎる場合は復元しない`() {
        val bounds = NarratorOverlayWindowBounds(x = 1900, y = 1060, width = 480, height = 120)

        assertNull(bounds.restorablePosition(listOf(primaryScreen)))
    }

    @Test
    fun `モニタ情報が取得できない場合は復元しない`() {
        val bounds = NarratorOverlayWindowBounds(x = 100, y = 100, width = 480, height = 120)

        assertNull(bounds.restorablePosition(emptyList()))
    }

    @Test
    fun `現在のモニタ領域はヘッドレス環境では空・GUI環境では1枚以上`() {
        val screenBounds = currentScreenBounds()

        if (GraphicsEnvironment.isHeadless()) {
            assertTrue(screenBounds.isEmpty())
        } else {
            assertTrue(screenBounds.isNotEmpty())
        }
    }
}
