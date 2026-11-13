package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AutoReadoutListItemVisualStyleTest {
    init {
        registerAllReadoutItemKeys()
    }

    @Test
    fun `カテゴリなしのONは順位と帯とタイルをprimary系の有効色で表示する`() {
        for (colors in listOf(lightColorScheme(), darkColorScheme())) {
            val style = autoReadoutListItemVisualStyle(true, colors, null)

            assertEquals(colors.onSurface, style.rankColor)
            assertEquals(colors.primary, style.accentColor)
            assertEquals(1f, style.accentAlpha)
            assertEquals(colors.primaryContainer, style.tileColor)
            assertEquals(colors.onPrimaryContainer, style.tileContentColor)
            assertNull(style.switchTrackColor)
        }
    }

    @Test
    fun `カテゴリありのONは帯とタイルとスイッチをカテゴリ色で表示する`() {
        val category = Color(0xFFC2570C)
        for (colors in listOf(lightColorScheme(), darkColorScheme())) {
            val style = autoReadoutListItemVisualStyle(true, colors, category)

            assertEquals(colors.onSurface, style.rankColor)
            assertEquals(category, style.accentColor)
            assertEquals(1f, style.accentAlpha)
            assertEquals(category.copy(alpha = CATEGORY_TILE_ALPHA), style.tileColor)
            assertEquals(category, style.tileContentColor)
            assertEquals(category, style.switchTrackColor)
        }
    }

    @Test
    fun `OFFはカテゴリの有無に関係なく順位を補助色にして帯を隠しタイルを無効色で表示する`() {
        for (colors in listOf(lightColorScheme(), darkColorScheme())) {
            for (category in listOf(null, Color(0xFFC2570C))) {
                val style = autoReadoutListItemVisualStyle(false, colors, category)

                assertEquals(colors.onSurfaceVariant, style.rankColor)
                assertEquals(category ?: colors.primary, style.accentColor)
                assertEquals(0f, style.accentAlpha)
                assertEquals(colors.surfaceContainerHighest, style.tileColor)
                assertEquals(colors.onSurfaceVariant, style.tileContentColor)
                assertNull(style.switchTrackColor)
            }
        }
    }
}
