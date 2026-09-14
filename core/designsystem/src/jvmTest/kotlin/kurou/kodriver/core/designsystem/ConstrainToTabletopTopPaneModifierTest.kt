package kurou.kodriver.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class ConstrainToTabletopTopPaneModifierTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `祖先がfillMaxSizeで高さをtightに固定していてもヒンジ上端までコンテンツの高さを縮める`() {
        val hingeTopDp = 100.dp
        rule.setContent {
            val hingeTopPx = with(androidx.compose.ui.platform.LocalDensity.current) { hingeTopDp.toPx() }
            val posture =
                Posture(
                    isTabletop = true,
                    hingeList =
                        listOf(
                            HingeInfo(
                                bounds = Rect(left = 0f, top = hingeTopPx, right = 300f, bottom = hingeTopPx + 20f),
                                isFlat = true,
                                isVertical = false,
                                isSeparating = true,
                                isOccluding = false,
                            ),
                        ),
                )
            // 祖先の fillMaxSize() が親の高さをそのまま子へ tight な制約として伝えるケースを再現する。
            Box(modifier = Modifier.size(width = 300.dp, height = 300.dp)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .constrainToTabletopTopPane(posture)
                                .testTag("tabletopTopPane"),
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Red))
                    }
                }
            }
        }

        rule.onNodeWithTag("tabletopTopPane").assertHeightIsEqualTo(hingeTopDp)
    }
}
