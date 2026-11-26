package kurou.kodriver.domain.serverconnection.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.serverconnection.repository.ConsoleAddressPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerConnectionObserveSettingsUseCasesTest {
    @Test
    fun `ObserveConsoleAddressUseCaseはRepositoryの監視Flowの値をそのまま返す`() {
        assertObserves<ConsoleAddressPreferencesRepository, String?>(
            "ObserveConsoleAddressUseCase",
            "192.168.1.100",
            observe = { consoleAddress() },
            useCase = { ObserveConsoleAddressUseCase(it)() },
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
