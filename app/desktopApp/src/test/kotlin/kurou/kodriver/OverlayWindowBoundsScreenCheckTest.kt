package kurou.kodriver

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kurou.kodriver.presentation.NarratorOverlayWindowBounds
import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OverlayWindowBoundsScreenCheckTest {
    private val primaryScreen = Rectangle(0, 0, 1920, 1080)
    private val secondaryScreen = Rectangle(-1920, 0, 1920, 1080)
    private val graphicsEnvironment: GraphicsEnvironment = mockk()
    private val primaryDevice: GraphicsDevice = mockk()
    private val secondaryDevice: GraphicsDevice = mockk()
    private val primaryConfiguration: GraphicsConfiguration = mockk()
    private val secondaryConfiguration: GraphicsConfiguration = mockk()

    @AfterTest
    fun tearDown() {
        unmockkStatic(GraphicsEnvironment::class)
    }

    @Test
    fun `位置が未設定なら復元しない`() {
        val bounds = NarratorOverlayWindowBounds(x = null, y = null, width = 480, height = 120)

        assertNull(bounds.restorablePosition(listOf(primaryScreen)))
    }

    @Test
    fun `xだけ未設定なら復元しない`() {
        val bounds = NarratorOverlayWindowBounds(x = null, y = 100, width = 480, height = 120)

        assertNull(bounds.restorablePosition(listOf(primaryScreen)))
    }

    @Test
    fun `yだけ未設定なら復元しない`() {
        val bounds = NarratorOverlayWindowBounds(x = 100, y = null, width = 480, height = 120)

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
    fun `横の重なりは十分でも縦の重なりが40px未満なら復元しない`() {
        val bounds = NarratorOverlayWindowBounds(x = 100, y = 1060, width = 480, height = 120)

        assertNull(bounds.restorablePosition(listOf(primaryScreen)))
    }

    @Test
    fun `縦の重なりは十分でも横の重なりが80px未満なら復元しない`() {
        val bounds = NarratorOverlayWindowBounds(x = 1900, y = 100, width = 480, height = 120)

        assertNull(bounds.restorablePosition(listOf(primaryScreen)))
    }

    @Test
    fun `モニタ情報が取得できない場合は復元しない`() {
        val bounds = NarratorOverlayWindowBounds(x = 100, y = 100, width = 480, height = 120)

        assertNull(bounds.restorablePosition(emptyList()))
    }

    @Test
    fun `ヘッドレス環境では現在のモニタ領域は空になる`() {
        mockkStatic(GraphicsEnvironment::class)
        every { GraphicsEnvironment.isHeadless() } returns true

        assertEquals(emptyList(), currentScreenBounds())
    }

    @Test
    fun `GUI環境では接続中の全モニタの領域を返す`() {
        mockkStatic(GraphicsEnvironment::class)
        every { GraphicsEnvironment.isHeadless() } returns false
        every { GraphicsEnvironment.getLocalGraphicsEnvironment() } returns graphicsEnvironment
        every { graphicsEnvironment.screenDevices } returns arrayOf(primaryDevice, secondaryDevice)
        every { primaryDevice.defaultConfiguration } returns primaryConfiguration
        every { secondaryDevice.defaultConfiguration } returns secondaryConfiguration
        every { primaryConfiguration.bounds } returns primaryScreen
        every { secondaryConfiguration.bounds } returns secondaryScreen

        assertEquals(listOf(primaryScreen, secondaryScreen), currentScreenBounds())
    }
}
