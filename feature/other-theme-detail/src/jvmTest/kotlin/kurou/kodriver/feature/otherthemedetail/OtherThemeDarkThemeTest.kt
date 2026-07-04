package kurou.kodriver.feature.otherthemedetail

import kurou.kodriver.domain.model.ThemeMode
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OtherThemeDarkThemeTest {
    @Test
    fun `SYSTEMはシステムのダーク状態を使う`() {
        assertTrue(resolveDarkTheme(ThemeMode.SYSTEM, systemDarkTheme = true))
        assertFalse(resolveDarkTheme(ThemeMode.SYSTEM, systemDarkTheme = false))
    }

    @Test
    fun `LIGHTはシステムのダーク状態に関係なくfalse`() {
        assertFalse(resolveDarkTheme(ThemeMode.LIGHT, systemDarkTheme = true))
        assertFalse(resolveDarkTheme(ThemeMode.LIGHT, systemDarkTheme = false))
    }

    @Test
    fun `DARKはシステムのダーク状態に関係なくtrue`() {
        assertTrue(resolveDarkTheme(ThemeMode.DARK, systemDarkTheme = true))
        assertTrue(resolveDarkTheme(ThemeMode.DARK, systemDarkTheme = false))
    }
}
