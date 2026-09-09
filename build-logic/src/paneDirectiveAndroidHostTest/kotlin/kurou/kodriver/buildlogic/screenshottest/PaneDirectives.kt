package kurou.kodriver.buildlogic.screenshottest

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
val twoPaneDirective =
    PaneScaffoldDirective(
        maxHorizontalPartitions = 2,
        horizontalPartitionSpacerSize = 16.dp,
        maxVerticalPartitions = 1,
        verticalPartitionSpacerSize = 0.dp,
        defaultPanePreferredWidth = 360.dp,
        excludedBounds = emptyList(),
    )

/**
 * 折りたたみ端末の展開状態（画面中央の縦ヒンジ）をシミュレートした [PaneScaffoldDirective] を返す。
 * list/detail 2ペイン構成の画面が、ヒンジ帯（[PaneScaffoldDirective.excludedBounds]）を避けて
 * レイアウトされることを確認するスクリーンショットテストで使用する。
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberFoldedVerticalHingeDirective(
    windowWidthDp: Float = 840f,
    windowHeightDp: Float = 640f,
    hingeWidthDp: Float = 20f,
): PaneScaffoldDirective {
    val density = LocalDensity.current
    return remember(density, windowWidthDp, windowHeightDp, hingeWidthDp) {
        val hingeCenterDp = windowWidthDp / 2f
        calculatePaneScaffoldDirective(
            WindowAdaptiveInfo(
                windowSizeClass = WindowSizeClass.compute(windowWidthDp, windowHeightDp),
                windowPosture =
                    Posture(
                        hingeList =
                            listOf(
                                HingeInfo(
                                    bounds =
                                        with(density) {
                                            Rect(
                                                left = (hingeCenterDp - hingeWidthDp / 2f).dp.toPx(),
                                                top = 0f,
                                                right = (hingeCenterDp + hingeWidthDp / 2f).dp.toPx(),
                                                bottom = windowHeightDp.dp.toPx(),
                                            )
                                        },
                                    isFlat = true,
                                    isVertical = true,
                                    isSeparating = true,
                                    isOccluding = false,
                                ),
                            ),
                    ),
            ),
        )
    }
}
