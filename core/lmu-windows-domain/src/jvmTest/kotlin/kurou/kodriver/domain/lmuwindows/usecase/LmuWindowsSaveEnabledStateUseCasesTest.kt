package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test

class LmuWindowsSaveEnabledStateUseCasesTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerLmuWindowsReadoutItemKeys()
    }

    @Test
    fun `有効状態のSave系UseCaseはリポジトリの保存メソッドへ委譲する`() {
        assertSaves<LmuWindowsFlagPreferencesRepository>(
            "SaveLmuWindowsFlagEnabledStateUseCase",
            save = { saveFlagEnabledState(LmuWindowsReadoutItemKey.Flag.RedFlag, false) },
            useCase = { SaveLmuWindowsFlagEnabledStateUseCase(it)(LmuWindowsReadoutItemKey.Flag.RedFlag, false) },
        )
        assertSaves<LmuWindowsPitTimingPreferencesRepository>(
            "SaveLmuWindowsPitTimingEnabledStateUseCase",
            save = { saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy, false) },
            useCase = {
                SaveLmuWindowsPitTimingEnabledStateUseCase(it)(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy, false)
            },
        )
        assertSaves<LmuWindowsTyreTemperaturePreferencesRepository>(
            "SaveLmuWindowsTyreTemperatureEnabledStateUseCase",
            save = { saveEnabledState(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning, true) },
            useCase = {
                SaveLmuWindowsTyreTemperatureEnabledStateUseCase(it)(
                    LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning,
                    true,
                )
            },
        )
        assertSaves<LmuWindowsVehicleApproachPreferencesRepository>(
            "SaveLmuWindowsVehicleApproachEnabledStateUseCase",
            save = { saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, false) },
            useCase = {
                SaveLmuWindowsVehicleApproachEnabledStateUseCase(it)(
                    LmuWindowsReadoutItemKey.VehicleApproach.Sustained,
                    false,
                )
            },
        )
        assertSaves<LmuWindowsVehicleDamagePreferencesRepository>(
            "SaveLmuWindowsVehicleDamageEnabledStateUseCase",
            save = { saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Overheat, true) },
            useCase = {
                SaveLmuWindowsVehicleDamageEnabledStateUseCase(
                    it,
                )(LmuWindowsReadoutItemKey.VehicleDamage.Overheat, true)
            },
        )
    }

    @Test
    fun `タイヤ温度低温警告フェーズのSaveUseCaseはリポジトリの保存メソッドへ委譲する`() {
        val phases = setOf(SessionPhase.WARM_UP, SessionPhase.FORMATION)
        assertSaves<LmuWindowsTyreTemperaturePreferencesRepository>(
            "SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase",
            save = { saveLowWarningPhases(phases) },
            useCase = { SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(it)(phases) },
        )
    }
}
