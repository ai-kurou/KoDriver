package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius
import kotlin.test.Test

class LmuWindowsSaveThresholdUseCasesTest {
    @Test
    fun `閾値のSave系UseCaseはリポジトリの保存メソッドへ委譲する`() {
        assertSaves<LmuWindowsTyreWearPreferencesRepository>(
            "SaveLmuWindowsTyreWearThresholdPercentageUseCase",
            save = { saveThresholdPercentage(30) },
            useCase = { SaveLmuWindowsTyreWearThresholdPercentageUseCase(it)(30) },
        )
        assertSaves<LmuWindowsRemainingVirtualEnergyPreferencesRepository>(
            "SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase",
            save = { saveThresholdPercentage(20) },
            useCase = { SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(it)(20) },
        )
        assertSaves<LmuWindowsRemainingFuelPreferencesRepository>(
            "SaveLmuWindowsRemainingFuelThresholdPercentageUseCase",
            save = { saveThresholdPercentage(20) },
            useCase = { SaveLmuWindowsRemainingFuelThresholdPercentageUseCase(it)(20) },
        )
        assertSaves<LmuWindowsVehicleClassBrakeWearPreferencesRepository>(
            "SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase",
            save = { saveLowThresholdPercent(LmuWindowsVehicleClassData.Gte, 25) },
            useCase = {
                SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase(
                    it,
                )(LmuWindowsVehicleClassData.Gte, 25)
            },
        )
        assertSaves<LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository>(
            "SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase",
            save = { saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 800) },
            useCase = {
                SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(
                    it,
                )(LmuWindowsVehicleClassData.Gte, 800)
            },
        )
        assertSaves<LmuWindowsVehicleClassTyreTemperaturePreferencesRepository>(
            "SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase",
            save = { saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, Celsius(100)) },
            useCase = {
                SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(
                    it,
                )(LmuWindowsVehicleClassData.Gte, Celsius(100))
            },
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
