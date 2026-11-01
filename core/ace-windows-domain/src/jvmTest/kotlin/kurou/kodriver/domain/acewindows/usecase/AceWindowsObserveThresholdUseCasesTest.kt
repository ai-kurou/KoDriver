package kurou.kodriver.domain.acewindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsObserveThresholdUseCasesTest {
    @Test
    fun `閾値のObserve系UseCaseはリポジトリの値をそのまま返す`() {
        assertObserves<AceWindowsTyreTemperaturePreferencesRepository, _>(
            "ObserveAceWindowsTyreTemperatureHighThresholdUseCase",
            Celsius(90),
            observe = { observeHighThresholdCelsius() },
            useCase = { ObserveAceWindowsTyreTemperatureHighThresholdUseCase(it)() },
        )
        assertObserves<AceWindowsRemainingFuelLapsPreferencesRepository, _>(
            "ObserveAceWindowsRemainingFuelLapsThresholdUseCase",
            5,
            observe = { observeThresholdLaps() },
            useCase = { ObserveAceWindowsRemainingFuelLapsThresholdUseCase(it)() },
        )
        assertObserves<AceWindowsRemainingFuelPreferencesRepository, _>(
            "ObserveAceWindowsRemainingFuelThresholdPercentageUseCase",
            30,
            observe = { observeThresholdPercentage() },
            useCase = { ObserveAceWindowsRemainingFuelThresholdPercentageUseCase(it)() },
        )
    }

    private inline fun <reified R : Any, T> assertObserves(
        useCaseName: String,
        expected: T,
        crossinline observe: R.() -> Flow<T>,
        crossinline useCase: (R) -> Flow<T>,
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
}
