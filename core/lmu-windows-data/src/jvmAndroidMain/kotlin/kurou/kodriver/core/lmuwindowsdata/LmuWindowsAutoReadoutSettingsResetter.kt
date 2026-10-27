package kurou.kodriver.core.lmuwindowsdata

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.AutoReadoutSettingsResetter

// 自動読み上げ設定の各 DataStore を明示的に列挙する。
@Suppress("LongParameterList")
internal class LmuWindowsAutoReadoutSettingsResetter(
    private val flagPreferencesRepository: LmuWindowsFlagPreferencesRepository,
    private val flagReadoutTextPreferencesRepository: LmuWindowsFlagReadoutTextPreferencesRepository,
    private val myBestLapPreferencesRepository: LmuWindowsMyBestLapPreferencesRepository,
    private val pitTimingPreferencesRepository: LmuWindowsPitTimingPreferencesRepository,
    private val remainingFuelPreferencesRepository: LmuWindowsRemainingFuelPreferencesRepository,
    private val remainingVirtualEnergyPreferencesRepository: LmuWindowsRemainingVirtualEnergyPreferencesRepository,
    private val tyreTemperaturePreferencesRepository: LmuWindowsTyreTemperaturePreferencesRepository,
    private val tyreWearPreferencesRepository: LmuWindowsTyreWearPreferencesRepository,
    private val vehicleApproachPreferencesRepository: LmuWindowsVehicleApproachPreferencesRepository,
    private val vehicleApproachThresholdsPreferencesRepository:
        LmuWindowsVehicleApproachThresholdsPreferencesRepository,
    private val vehicleClassBrakeTemperaturePreferencesRepository:
        LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository,
    private val vehicleClassBrakeWearPreferencesRepository: LmuWindowsVehicleClassBrakeWearPreferencesRepository,
    private val vehicleClassTyreTemperaturePreferencesRepository:
        LmuWindowsVehicleClassTyreTemperaturePreferencesRepository,
    private val vehicleDamagePreferencesRepository: LmuWindowsVehicleDamagePreferencesRepository,
) : AutoReadoutSettingsResetter {
    override suspend fun resetToDefault() {
        flagPreferencesRepository.resetToDefault()
        flagReadoutTextPreferencesRepository.resetToDefault()
        myBestLapPreferencesRepository.resetToDefault()
        pitTimingPreferencesRepository.resetToDefault()
        remainingFuelPreferencesRepository.resetToDefault()
        remainingVirtualEnergyPreferencesRepository.resetToDefault()
        tyreTemperaturePreferencesRepository.resetToDefault()
        tyreWearPreferencesRepository.resetToDefault()
        vehicleApproachPreferencesRepository.resetToDefault()
        vehicleApproachThresholdsPreferencesRepository.resetToDefault()
        vehicleClassBrakeTemperaturePreferencesRepository.resetToDefault()
        vehicleClassBrakeWearPreferencesRepository.resetToDefault()
        vehicleClassTyreTemperaturePreferencesRepository.resetToDefault()
        vehicleDamagePreferencesRepository.resetToDefault()
    }
}
