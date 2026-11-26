package kurou.kodriver.domain.telemetrylog.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.telemetrylog.repository.TelemetryLogRepository
import kotlin.test.Test

class TelemetryLogActionUseCasesTest {
    @Test
    fun `suspend系の操作UseCaseはRepositoryの対応するメソッドへ委譲する`() =
        runTest {
            assertDelegatesSuspend<TelemetryLogRepository>(
                useCaseName = "DeleteTelemetryLogUseCase",
                call = { deleteTelemetryLog(1L) },
                useCase = { DeleteTelemetryLogUseCase(it)(1L) },
            )
        }

    private suspend inline fun <reified R : Any> assertDelegatesSuspend(
        useCaseName: String,
        crossinline call: suspend R.() -> Unit,
        crossinline useCase: suspend (R) -> Unit,
    ) {
        val repository = mockk<R>(relaxUnitFun = true)
        try {
            useCase(repository)

            coVerify(exactly = 1) { repository.call() }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
    }
}
