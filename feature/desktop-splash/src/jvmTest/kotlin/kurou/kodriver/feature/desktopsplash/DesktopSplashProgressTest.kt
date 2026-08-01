package kurou.kodriver.feature.desktopsplash

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DesktopSplashProgressTest {
    @Test
    fun `初期状態はモジュール初期化フェーズ`() =
        runTest {
            val progress = DesktopSplashProgress()

            assertEquals(DesktopSplashStep.INITIALIZING_MODULES, progress.uiState.first().step)
        }

    @Test
    fun `update で現在のフェーズが更新される`() =
        runTest {
            val progress = DesktopSplashProgress()

            progress.update(DesktopSplashStep.STARTING_SERVER)

            assertEquals(DesktopSplashStep.STARTING_SERVER, progress.uiState.first().step)
        }
}
