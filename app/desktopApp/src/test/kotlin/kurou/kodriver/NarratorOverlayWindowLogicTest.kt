package kurou.kodriver

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import kurou.kodriver.presentation.NarratorOverlayWindowBounds
import java.awt.Point
import java.awt.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NarratorOverlayWindowLogicTest {
    private val bounds = NarratorOverlayWindowBounds(x = 120, y = 80, width = 480, height = 120)
    private val screen = Rectangle(0, 0, 1920, 1080)

    @Test
    fun `現在の画面内に位置があれば絶対位置として復元する`() {
        val position = bounds.initialWindowPosition(listOf(screen))

        assertEquals(WindowPosition.Absolute(120.dp, 80.dp), position)
    }

    @Test
    fun `保存位置が復元できなければ画面上部中央を使う`() {
        val position = bounds.initialWindowPosition(emptyList())

        assertEquals(WindowPosition.Aligned(Alignment.TopCenter), position)
    }

    @Test
    fun `絶対位置とサイズを四捨五入した保存用geometryに変換する`() {
        val geometry =
            narratorOverlayWindowGeometry(
                WindowPosition.Absolute(120.4.dp, 80.6.dp),
                DpSize(480.4.dp, 120.6.dp),
            )

        assertEquals(NarratorOverlayWindowGeometry(x = 120, y = 81, width = 480, height = 121), geometry)
    }

    @Test
    fun `絶対位置が決まる前は保存用geometryを作らない`() {
        val geometry =
            narratorOverlayWindowGeometry(
                WindowPosition.Aligned(Alignment.TopCenter),
                DpSize(480.dp, 120.dp),
            )

        assertNull(geometry)
    }

    @Test
    fun `ドラッグ開始からのスクリーン座標差分でウィンドウ位置を計算する`() {
        val location =
            draggedWindowLocation(
                startWindowLocation = Point(100, 200),
                startPointerLocation = Point(500, 600),
                currentPointerLocation = Point(530, 560),
            )

        assertEquals(Point(130, 160), location)
    }

    @Test
    fun `ドラッグ座標のいずれかが取得できなければ位置を変更しない`() {
        assertNull(draggedWindowLocation(null, Point(0, 0), Point(1, 1)))
        assertNull(draggedWindowLocation(Point(0, 0), null, Point(1, 1)))
        assertNull(draggedWindowLocation(Point(0, 0), Point(0, 0), null))
    }

    @Test
    fun `表示が有効かつboundsの読み込み済みの場合だけboundsを返す`() {
        assertEquals(bounds, narratorOverlayBoundsToDisplay(visible = true, bounds = bounds))
        assertNull(narratorOverlayBoundsToDisplay(visible = false, bounds = bounds))
        assertNull(narratorOverlayBoundsToDisplay(visible = null, bounds = bounds))
        assertNull(narratorOverlayBoundsToDisplay(visible = true, bounds = null))
    }
}
