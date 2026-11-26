package kurou.kodriver.domain.overlay.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.overlay.model.OverlayTextSize
import kurou.kodriver.domain.overlay.model.OverlayWindowBounds
import kurou.kodriver.domain.overlay.repository.OverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.domain.overlay.repository.OverlayTextColorPreferencesRepository
import kurou.kodriver.domain.overlay.repository.OverlayTextSizePreferencesRepository
import kurou.kodriver.domain.overlay.repository.OverlayVisiblePreferencesRepository
import kurou.kodriver.domain.overlay.repository.OverlayWindowBoundsPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class OverlayObserveSettingsUseCasesTest {
    @Test
    fun `Observe系UseCaseはRepositoryの監視Flowの値をそのまま返す`() {
        assertObserves<OverlayTextSizePreferencesRepository, OverlayTextSize>(
            "ObserveOverlayTextSizeUseCase",
            OverlayTextSize.LARGE,
            observe = { observeOverlayTextSize() },
            useCase = { ObserveOverlayTextSizeUseCase(it)() },
        )
        assertObserves<OverlayVisiblePreferencesRepository, Boolean>(
            "ObserveOverlayVisibleUseCase",
            true,
            observe = { observeOverlayVisible() },
            useCase = { ObserveOverlayVisibleUseCase(it)() },
        )
        assertObserves<OverlayBackgroundOpacityPreferencesRepository, Int>(
            "ObserveOverlayBackgroundOpacityUseCase",
            80,
            observe = { observeOverlayBackgroundOpacity() },
            useCase = { ObserveOverlayBackgroundOpacityUseCase(it)() },
        )
        assertObserves<OverlayTextColorPreferencesRepository, Long>(
            "ObserveOverlayTextColorUseCase",
            0xFF123456L,
            observe = { observeOverlayTextColor() },
            useCase = { ObserveOverlayTextColorUseCase(it)() },
        )
        assertObserves<OverlayWindowBoundsPreferencesRepository, OverlayWindowBounds>(
            "ObserveOverlayWindowBoundsUseCase",
            OverlayWindowBounds(x = 10, y = 20, width = 300, height = 100),
            observe = { observeOverlayWindowBounds() },
            useCase = { ObserveOverlayWindowBoundsUseCase(it)() },
        )
    }

    private inline fun <reified R : Any, T> assertObserves(
        useCaseName: String,
        expected: T,
        crossinline observe: R.() -> Flow<T>,
        crossinline useCase: (R) -> Flow<T>,
    ) {
        val repository = mockk<R>()
        every { repository.observe() } returns flowOf(expected)
        try {
            runTest {
                assertEquals(expected, useCase(repository).first())
            }
            verify(exactly = 1) { repository.observe() }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
    }
}
