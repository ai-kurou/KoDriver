package kurou.kodriver.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ThemeModeTest {
    @Test
    fun `idからテーマモードへ変換できる`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromId("system"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromId("light"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromId("dark"))
    }

    @Test
    fun `未知のidはnullになる`() {
        assertNull(ThemeMode.fromId("unknown"))
    }
}
