package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutItemKey

/**
 * 背景画像を持つ項目の詳細ペインを、本番と同じタイトル（[autoReadoutItemDisplayName]）・背景（[autoReadoutBackground]）で
 * 描画して撮影する。
 */
internal fun captureDetailPane(
    item: ReadoutItemKey,
    dark: Boolean,
) = composeScreenshotTest {
    setContent {
        KoDriverTheme(darkTheme = dark) {
            Surface {
                Box(modifier = Modifier.requiredSize(480.dp, 160.dp)) {
                    AutoReadoutDetailPane(
                        title = autoReadoutItemDisplayName(item),
                        canNavigateBack = true,
                        onBack = {},
                        background = autoReadoutBackground(item),
                    ) {}
                }
            }
        }
    }
    onRoot().captureRoboImage()
}
