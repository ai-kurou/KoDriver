package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test

private val EMPHASIS_LEVELS = listOf(0f, 0.5f, 1f)

/**
 * 読み上げの決め手になった語句の点滅は時間で変わるため、強調の度合いを固定した状態を並べて撮る。
 * 左から強調なし・途中・最大。アニメーションそのものは撮らない。
 */
class KoDriverPhraseChipScreenshotTest {
    @Test
    fun `強調の度合い_アクセント色あり`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 96.dp)) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                EMPHASIS_LEVELS.forEach {
                                    KoDriverPhraseChip(
                                        "燃料残量",
                                        accent = Color(0xFFE53935),
                                        emphasis = it,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `強調の度合い_アクセント色なし`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 96.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    EMPHASIS_LEVELS.forEach { KoDriverPhraseChip("燃料残量", emphasis = it) }
                                }
                            }
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }
}
