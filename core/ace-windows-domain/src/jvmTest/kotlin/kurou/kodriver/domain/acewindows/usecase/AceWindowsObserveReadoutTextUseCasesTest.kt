package kurou.kodriver.domain.acewindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsObserveReadoutTextUseCasesTest {
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
        assertObserves<AceWindowsMyBestLapPreferencesRepository>(
            useCaseName = "ObserveAceWindowsMyBestLapReadoutTextUseCase",
            expected = "更新{laptime}",
            observe = { observeReadoutText() },
            useCase = { ObserveAceWindowsMyBestLapReadoutTextUseCase(it)() },
        )
    }

    @Test
    fun `各ObserveUseCaseがRepositoryの値をそのまま流すその2`() {
        assertObserves<AceWindowsRemainingFuelLapsPreferencesRepository>(
            useCaseName = "ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase",
            expected = "燃料なし",
            observe = { observeEmptyReadoutText() },
            useCase = { ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase(it)() },
        )

        assertObserves<AceWindowsRemainingFuelLapsPreferencesRepository>(
            useCaseName = "ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase",
            expected = "残り{laps}周",
            observe = { observeReadoutText() },
            useCase = { ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase(it)() },
        )

        assertObserves<AceWindowsRemainingFuelPreferencesRepository>(
            useCaseName = "ObserveAceWindowsRemainingFuelReadoutTextUseCase",
            expected = "残り{percent}パーセント",
            observe = { observeReadoutText() },
            useCase = { ObserveAceWindowsRemainingFuelReadoutTextUseCase(it)() },
        )

        assertObserves<AceWindowsTyreTemperaturePreferencesRepository>(
            useCaseName = "ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase",
            expected = "タイヤが過熱しています",
            observe = { observeOverheatReadoutText() },
            useCase = { ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase(it)() },
        )

        assertObserves<AceWindowsVehicleApproachPreferencesRepository>(
            useCaseName = "ObserveAceWindowsVehicleApproachReadoutTextUseCase",
            expected = "車両接近",
            observe = { observeReadoutText() },
            useCase = { ObserveAceWindowsVehicleApproachReadoutTextUseCase(it)() },
        )
    }
}
