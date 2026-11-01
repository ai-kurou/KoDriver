package kurou.kodriver.domain.gt7ps5.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5ObserveReadoutTextUseCasesTest {
    private inline fun <reified R : Any> assertObserves(
        useCaseName: String,
        expected: String,
        crossinline observe: R.() -> Flow<String>,
        crossinline useCase: (R) -> Flow<String>,
    ) = runTest {
        val repository = mockk<R>()
        every { repository.observe() } returns flowOf(expected)

        try {
            assertEquals(expected, useCase(repository).first())
            verify(exactly = 1) { repository.observe() }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
    }

    @Test
    fun `各ObserveUseCaseがRepositoryの値をそのまま流すその1`() {
        assertObserves<Gt7Ps5MyBestLapPreferencesRepository>(
            useCaseName = "ObserveGt7Ps5MyBestLapReadoutTextUseCase",
            expected = "更新{laptime}",
            observe = { observeReadoutText() },
            useCase = { ObserveGt7Ps5MyBestLapReadoutTextUseCase(it)() },
        )

        assertObserves<Gt7Ps5RemainingFuelLapsPreferencesRepository>(
            useCaseName = "ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase",
            expected = "燃料なし",
            observe = { observeEmptyReadoutText() },
            useCase = { ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase(it)() },
        )

        assertObserves<Gt7Ps5RemainingFuelLapsPreferencesRepository>(
            useCaseName = "ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase",
            expected = "残り{laps}周",
            observe = { observeReadoutText() },
            useCase = { ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase(it)() },
        )

        assertObserves<Gt7Ps5RemainingFuelPreferencesRepository>(
            useCaseName = "ObserveGt7Ps5RemainingFuelReadoutTextUseCase",
            expected = "残り{percent}パーセント",
            observe = { observeReadoutText() },
            useCase = { ObserveGt7Ps5RemainingFuelReadoutTextUseCase(it)() },
        )

        assertObserves<Gt7Ps5TyreTemperaturePreferencesRepository>(
            useCaseName = "ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase",
            expected = "タイヤが過熱しています",
            observe = { observeOverheatReadoutText() },
            useCase = { ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase(it)() },
        )
    }
}
