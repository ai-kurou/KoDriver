package kurou.kodriver.domain.manualreadout.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.manualreadout.model.ManualReadoutRequest
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutRequestRepository
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test

class ManualReadoutActionUseCasesTest {
    @Test
    fun `suspend系の操作UseCaseはRepositoryの対応するメソッドへ委譲する`() =
        runTest {
            assertDelegatesSuspend<ManualReadoutSetPreferencesRepository>(
                useCaseName = "AddManualReadoutSetUseCase",
                call = { addSet(ManualReadoutSet("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR"))) },
                useCase = {
                    AddManualReadoutSetUseCase(
                        it,
                    )(ManualReadoutSet("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR")))
                },
            )
            assertDelegatesSuspend<ManualReadoutSetPreferencesRepository>(
                useCaseName = "UpdateManualReadoutSetUseCase",
                call = { updateSet(ManualReadoutSet("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR"))) },
                useCase = {
                    UpdateManualReadoutSetUseCase(
                        it,
                    )(ManualReadoutSet("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR")))
                },
            )
            assertDelegatesSuspend<ManualReadoutSetPreferencesRepository>(
                useCaseName = "DeleteManualReadoutSetUseCase",
                call = { deleteSet("set-1") },
                useCase = { DeleteManualReadoutSetUseCase(it)("set-1") },
            )
            assertDelegatesSuspend<ManualReadoutRequestRepository>(
                useCaseName = "RequestManualReadoutUseCase",
                call = { request(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1")) },
                useCase = {
                    RequestManualReadoutUseCase(
                        it,
                    )(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))
                },
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
