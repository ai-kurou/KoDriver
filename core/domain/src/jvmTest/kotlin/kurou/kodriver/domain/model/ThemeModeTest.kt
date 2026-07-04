package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeModeTest {

    @Test
    fun `idからテーマモードへ変換できる`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromId("system"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromId("light"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromId("dark"))
    }

    @Test
    fun `未知のidはSYSTEMになる`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromId("unknown"))
    }
}
