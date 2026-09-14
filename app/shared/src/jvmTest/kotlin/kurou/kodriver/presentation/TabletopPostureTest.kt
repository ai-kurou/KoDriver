package kurou.kodriver.presentation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class TabletopPostureTest {
    @Test
    fun `テーブルトップ姿勢でない場合はnullを返す`() {
        val posture =
            Posture(
                isTabletop = false,
                hingeList =
                    listOf(
                        horizontalSeparatingHinge(top = 400f),
                    ),
            )

        assertNull(posture.tabletopTopPaneHeightPx())
    }

    @Test
    fun `テーブルトップ姿勢でヒンジ情報が無い場合はnullを返す`() {
        val posture = Posture(isTabletop = true, hingeList = emptyList())

        assertNull(posture.tabletopTopPaneHeightPx())
    }

    @Test
    fun `テーブルトップ姿勢で分離しない水平ヒンジしか無い場合はnullを返す`() {
        val posture =
            Posture(
                isTabletop = true,
                hingeList =
                    listOf(
                        horizontalSeparatingHinge(top = 400f, isSeparating = false),
                    ),
            )

        assertNull(posture.tabletopTopPaneHeightPx())
    }

    @Test
    fun `テーブルトップ姿勢で垂直ヒンジしか無い場合はnullを返す`() {
        val posture =
            Posture(
                isTabletop = true,
                hingeList =
                    listOf(
                        horizontalSeparatingHinge(top = 400f, isVertical = true),
                    ),
            )

        assertNull(posture.tabletopTopPaneHeightPx())
    }

    @Test
    fun `テーブルトップ姿勢で分離する水平ヒンジがある場合はヒンジ上端のpxを返す`() {
        val posture =
            Posture(
                isTabletop = true,
                hingeList =
                    listOf(
                        horizontalSeparatingHinge(top = 400f),
                    ),
            )

        assertEquals(400f, posture.tabletopTopPaneHeightPx())
    }

    @Test
    fun `複数のヒンジがある場合は最初に見つかった分離する水平ヒンジのpxを返す`() {
        val posture =
            Posture(
                isTabletop = true,
                hingeList =
                    listOf(
                        horizontalSeparatingHinge(top = 400f, isVertical = true),
                        horizontalSeparatingHinge(top = 500f),
                        horizontalSeparatingHinge(top = 600f),
                    ),
            )

        assertEquals(500f, posture.tabletopTopPaneHeightPx())
    }

    @Test
    fun `テーブルトップ姿勢の場合はshouldCollapseDetailPaneがtrueになる`() {
        val posture = Posture(isTabletop = true, hingeList = emptyList())

        assertTrue(posture.shouldCollapseDetailPane)
    }

    @Test
    fun `テーブルトップ姿勢でなく分離するヒンジも無い場合はshouldCollapseDetailPaneがfalseになる`() {
        val posture = Posture(isTabletop = false, hingeList = emptyList())

        assertFalse(posture.shouldCollapseDetailPane)
    }

    @Test
    fun `テーブルトップ姿勢でなく完全に平らな縦ヒンジのみの場合はshouldCollapseDetailPaneがfalseになる`() {
        val posture =
            Posture(
                isTabletop = false,
                hingeList = listOf(verticalSeparatingHinge(isFlat = true)),
            )

        assertFalse(posture.shouldCollapseDetailPane)
    }

    @Test
    fun `テーブルトップ姿勢でなく平らでない縦ヒンジがある場合はshouldCollapseDetailPaneがtrueになる`() {
        val posture =
            Posture(
                isTabletop = false,
                hingeList = listOf(verticalSeparatingHinge(isFlat = false)),
            )

        assertTrue(posture.shouldCollapseDetailPane)
    }

    @Test
    fun `平らでなくても分離しない縦ヒンジの場合はshouldCollapseDetailPaneがfalseになる`() {
        val posture =
            Posture(
                isTabletop = false,
                hingeList = listOf(verticalSeparatingHinge(isFlat = false, isSeparating = false)),
            )

        assertFalse(posture.shouldCollapseDetailPane)
    }

    private fun verticalSeparatingHinge(
        isFlat: Boolean,
        isSeparating: Boolean = true,
    ): HingeInfo =
        HingeInfo(
            bounds = Rect(left = 400f, top = 0f, right = 420f, bottom = 800f),
            isFlat = isFlat,
            isVertical = true,
            isSeparating = isSeparating,
            isOccluding = false,
        )

    private fun horizontalSeparatingHinge(
        top: Float,
        isVertical: Boolean = false,
        isSeparating: Boolean = true,
    ): HingeInfo =
        HingeInfo(
            bounds = Rect(left = 0f, top = top, right = 800f, bottom = top + 20f),
            isFlat = true,
            isVertical = isVertical,
            isSeparating = isSeparating,
            isOccluding = false,
        )
}
