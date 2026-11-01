package kurou.kodriver.feature.narratoroverlay

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.OverlayTextSize
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class NarratorOverlayTextStyleTest {
    @Test
    fun `すべての文字サイズに黒い影が適用される`() =
        runComposeUiTest {
            val styles = mutableMapOf<OverlayTextSize, TextStyle>()
            setContent {
                KoDriverTheme {
                    OverlayTextSize.entries.forEach { size ->
                        styles[size] = narratorOverlayTextStyle(size)
                    }
                }
            }

            runOnIdle {
                val expectedShadow =
                    Shadow(color = Color.Black.copy(alpha = 0.8f), offset = Offset(2f, 2f), blurRadius = 4f)
                OverlayTextSize.entries.forEach { size ->
                    assertEquals(expectedShadow, styles[size]?.shadow, size.name)
                }
            }
        }
}
