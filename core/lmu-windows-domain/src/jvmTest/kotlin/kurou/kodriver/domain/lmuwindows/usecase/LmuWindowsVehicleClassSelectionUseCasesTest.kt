package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kotlin.test.Test

class LmuWindowsVehicleClassSelectionUseCasesTest {
    @Test
    fun `選択クラスを監視するUseCaseはリポジトリの監視Flowを返す`() {
        assertObserves<LmuWindowsVehicleClassBrakeWearPreferencesRepository, LmuWindowsVehicleClassData>(
            "ObserveLmuWindowsVehicleClassBrakeWearSelectionUseCase",
            LmuWindowsVehicleClassData.Gte,
            observe = { observeSelectedVehicleClass() },
            useCase = { ObserveLmuWindowsVehicleClassBrakeWearSelectionUseCase(it)() },
        )
        assertObserves<LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository, LmuWindowsVehicleClassData>(
            "ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase",
            LmuWindowsVehicleClassData.Gte,
            observe = { observeSelectedVehicleClass() },
            useCase = { ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase(it)() },
        )
        assertObserves<LmuWindowsVehicleClassTyreTemperaturePreferencesRepository, LmuWindowsVehicleClassData>(
            "ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase",
            LmuWindowsVehicleClassData.Gte,
            observe = { observeSelectedVehicleClass() },
            useCase = { ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase(it)() },
        )
    }

    @Test
    fun `選択クラスを保存するUseCaseはリポジトリの保存メソッドへ委譲する`() {
        assertSaves<LmuWindowsVehicleClassBrakeWearPreferencesRepository>(
            "SaveLmuWindowsVehicleClassBrakeWearSelectionUseCase",
            save = { saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) },
            useCase = { SaveLmuWindowsVehicleClassBrakeWearSelectionUseCase(it)(LmuWindowsVehicleClassData.Gte) },
        )
        assertSaves<LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository>(
            "SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase",
            save = { saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) },
            useCase = {
                SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase(it)(LmuWindowsVehicleClassData.Gte)
            },
        )
        assertSaves<LmuWindowsVehicleClassTyreTemperaturePreferencesRepository>(
            "SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase",
            save = { saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) },
            useCase = {
                SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase(it)(LmuWindowsVehicleClassData.Gte)
            },
        )
    }
}
