package kurou.kodriver.domain.gt7ps5.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius
import kotlin.test.Test

class Gt7Ps5SaveThresholdUseCasesTest {
    @Test
    fun `閾値のSave系UseCaseはリポジトリの保存メソッドへ委譲する`() {
        assertSaves<Gt7Ps5RemainingFuelPreferencesRepository>(
            "SaveGt7Ps5RemainingFuelThresholdPercentageUseCase",
            save = { saveThresholdPercentage(45) },
            useCase = { SaveGt7Ps5RemainingFuelThresholdPercentageUseCase(it)(45) },
        )
        assertSaves<Gt7Ps5TyreTemperaturePreferencesRepository>(
            "SaveGt7Ps5TyreTemperatureHighThresholdUseCase",
            save = { saveHighThresholdCelsius(Celsius(95)) },
            useCase = { SaveGt7Ps5TyreTemperatureHighThresholdUseCase(it)(Celsius(95)) },
        )
    }

    private inline fun <reified R : Any> assertSaves(
        useCaseName: String,
        crossinline save: suspend R.() -> Unit,
        crossinline useCase: suspend (R) -> Unit,
    ) = runTest {
        val repository = mockk<R>(relaxUnitFun = true)
        try {
            useCase(repository)
            coVerify(exactly = 1) { repository.save() }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
    }
}
