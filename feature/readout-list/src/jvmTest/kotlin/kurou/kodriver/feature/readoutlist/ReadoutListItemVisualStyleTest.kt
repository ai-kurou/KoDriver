package kurou.kodriver.feature.readoutlist

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import org.junit.Test
import kotlin.test.assertEquals

class ReadoutListItemVisualStyleTest {
    @Test
    fun `ONは順位と帯とタイルを有効色で表示する`() {
        for (colors in listOf(lightColorScheme(), darkColorScheme())) {
            val style = readoutListItemVisualStyle(true, colors)

            assertEquals(colors.onSurface, style.rankColor)
            assertEquals(1f, style.accentAlpha)
            assertEquals(colors.primaryContainer, style.tileColor)
            assertEquals(colors.onPrimaryContainer, style.tileContentColor)
        }
    }

    @Test
    fun `OFFは順位を通常の補助色で表示して帯を隠しタイルを無効色で表示する`() {
        for (colors in listOf(lightColorScheme(), darkColorScheme())) {
            val style = readoutListItemVisualStyle(false, colors)

            assertEquals(colors.onSurfaceVariant, style.rankColor)
            assertEquals(0f, style.accentAlpha)
            assertEquals(colors.surfaceContainerHighest, style.tileColor)
            assertEquals(colors.onSurfaceVariant, style.tileContentColor)
        }
    }
}
