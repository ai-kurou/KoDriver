package kurou.kodriver.domain.acewindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius
import kotlin.test.Test

class AceWindowsSaveThresholdUseCasesTest {
    @Test
    fun `閾値のSave系UseCaseはリポジトリの保存メソッドへ委譲する`() {
        assertSaves<AceWindowsTyreTemperaturePreferencesRepository>(
            "SaveAceWindowsTyreTemperatureHighThresholdUseCase",
            save = { saveHighThresholdCelsius(Celsius(90)) },
            useCase = { SaveAceWindowsTyreTemperatureHighThresholdUseCase(it)(Celsius(90)) },
        )
        assertSaves<AceWindowsRemainingFuelLapsPreferencesRepository>(
            "SaveAceWindowsRemainingFuelLapsThresholdUseCase",
            save = { saveThresholdLaps(1) },
            useCase = { SaveAceWindowsRemainingFuelLapsThresholdUseCase(it)(1) },
        )
        assertSaves<AceWindowsRemainingFuelPreferencesRepository>(
            "SaveAceWindowsRemainingFuelThresholdPercentageUseCase",
            save = { saveThresholdPercentage(45) },
            useCase = { SaveAceWindowsRemainingFuelThresholdPercentageUseCase(it)(45) },
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
