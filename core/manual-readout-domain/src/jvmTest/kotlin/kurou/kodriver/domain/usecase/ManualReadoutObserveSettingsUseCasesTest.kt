package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutObserveSettingsUseCasesTest {
    @Test
    fun `Observe系UseCaseはRepositoryの監視Flowの値をそのまま返す`() {
        assertObserves<ManualReadoutSetPreferencesRepository, List<ManualReadoutSet>>(
            "ObserveManualReadoutSetsUseCase",
            listOf(ManualReadoutSet("set-1", listOf("SESSION", "SIMULATOR")), ManualReadoutSet("set-2")),
            observe = { observeSets() },
            useCase = { ObserveManualReadoutSetsUseCase(it)() },
        )
        assertObserves<ManualReadoutPlayButtonLayoutEnabledPreferencesRepository, Boolean>(
            "ObserveManualReadoutPlayButtonLayoutEnabledUseCase",
            true,
            observe = { observeManualReadoutPlayButtonLayoutEnabled() },
            useCase = { ObserveManualReadoutPlayButtonLayoutEnabledUseCase(it)() },
        )
        assertObserves<ManualReadoutRequestRepository, ManualReadoutRequest>(
            "ObserveManualReadoutRequestsUseCase",
            ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"),
            observe = { observeRequests() },
            useCase = { ObserveManualReadoutRequestsUseCase(it)() },
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
