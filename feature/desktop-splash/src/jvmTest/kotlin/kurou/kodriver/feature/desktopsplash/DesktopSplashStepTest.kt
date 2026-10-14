package kurou.kodriver.feature.desktopsplash

import kurou.kodriver.feature.desktopsplash.generated.resources.Res
import kurou.kodriver.feature.desktopsplash.generated.resources.desktop_splash_initializing_modules
import kurou.kodriver.feature.desktopsplash.generated.resources.desktop_splash_ready
import kurou.kodriver.feature.desktopsplash.generated.resources.desktop_splash_starting_server
import kotlin.test.Test
import kotlin.test.assertEquals

class DesktopSplashStepTest {
    @Test
    fun `各フェーズに対応する表示名の文字列リソースを返す`() {
        assertEquals(
            Res.string.desktop_splash_initializing_modules,
            DesktopSplashStep.INITIALIZING_MODULES.displayNameRes,
        )
        assertEquals(Res.string.desktop_splash_starting_server, DesktopSplashStep.STARTING_SERVER.displayNameRes)
        assertEquals(Res.string.desktop_splash_ready, DesktopSplashStep.READY.displayNameRes)
    }
}
