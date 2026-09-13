package kurou.kodriver.core.designsystem

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
