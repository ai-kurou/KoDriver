package kurou.kodriver.feature.desktopsplash

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DesktopSplashInitializerTest {

    @Test
    fun `フェーズを順に進めて最後にREADYへ遷移する`() =
        runTest {
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

    @Test
    fun `initializeModules が失敗した場合は例外を伝播しREADYへ遷移しない`() =
        runTest {
        val progress = DesktopSplashProgress()

        assertFailsWith<IllegalStateException> {
            progress.runInitialization(
                initializeModules = { error("init failed") },
                startServer = {},
            )
        }

        assertEquals(DesktopSplashStep.INITIALIZING_MODULES, progress.uiState.first().step)
    }

    @Test
    fun `startServer が失敗した場合は例外を伝播しSTARTING_SERVERで停止する`() =
        runTest {
        val progress = DesktopSplashProgress()

        assertFailsWith<IllegalStateException> {
            progress.runInitialization(
                initializeModules = {},
                startServer = { error("server failed") },
            )
        }

        assertEquals(DesktopSplashStep.STARTING_SERVER, progress.uiState.first().step)
    }
}
