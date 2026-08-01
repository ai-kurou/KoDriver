package kurou.kodriver.feature.desktopsplash

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopSplashUiStateTest {
    @Test
    fun `デフォルトはモジュール初期化フェーズで未完了`() {
        val state = DesktopSplashUiState()

        assertEquals(DesktopSplashStep.INITIALIZING_MODULES, state.step)
        assertFalse(state.isReady)
    }

    @Test
    fun `READY フェーズでは isReady が true になる`() {
        val state = DesktopSplashUiState(step = DesktopSplashStep.READY)

        assertTrue(state.isReady)
    }

    @Test
    fun `フェーズの順序に応じた進捗率を返す`() {
        assertEquals(1f / 3f, DesktopSplashUiState(DesktopSplashStep.INITIALIZING_MODULES).progress)
        assertEquals(2f / 3f, DesktopSplashUiState(DesktopSplashStep.STARTING_SERVER).progress)
        assertEquals(1f, DesktopSplashUiState(DesktopSplashStep.READY).progress)
    }
}
