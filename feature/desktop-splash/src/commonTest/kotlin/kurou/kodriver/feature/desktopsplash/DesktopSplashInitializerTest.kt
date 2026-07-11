package kurou.kodriver.feature.desktopsplash

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DesktopSplashInitializerTest {

    @Test
    fun `フェーズを順に進めて最後にREADYへ遷移する`() = runTest {
        val progress = DesktopSplashProgress()
        val observedSteps = mutableListOf<DesktopSplashStep>()

        progress.runInitialization(
            initializeModules = { observedSteps += progress.uiState.first().step },
            startServer = { observedSteps += progress.uiState.first().step },
        )

        assertEquals(
            listOf(DesktopSplashStep.INITIALIZING_MODULES, DesktopSplashStep.STARTING_SERVER),
            observedSteps,
        )
        assertEquals(DesktopSplashStep.READY, progress.uiState.first().step)
    }
}
