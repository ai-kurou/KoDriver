package kurou.kodriver.feature.desktopsplash

import kotlin.test.Test
import kotlin.test.assertEquals

class DesktopSplashStepTest {
    @Test
    fun `各フェーズに対応する日本語表示名を返す`() {
        assertEquals("モジュールを初期化しています…", DesktopSplashStep.INITIALIZING_MODULES.displayName)
        assertEquals("サーバーを起動しています…", DesktopSplashStep.STARTING_SERVER.displayName)
        assertEquals("起動が完了しました", DesktopSplashStep.READY.displayName)
    }
}
