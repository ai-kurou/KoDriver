package kurou.kodriver.core.acewindowsdata

import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.AutoReadoutSettingsResetter

internal class AceWindowsAutoReadoutSettingsResetter(
    private val flagPreferencesRepository: AceWindowsFlagPreferencesRepository,
    private val flagReadoutTextPreferencesRepository: AceWindowsFlagReadoutTextPreferencesRepository,
    private val myBestLapPreferencesRepository: AceWindowsMyBestLapPreferencesRepository,
    private val remainingFuelLapsPreferencesRepository: AceWindowsRemainingFuelLapsPreferencesRepository,
    private val remainingFuelPreferencesRepository: AceWindowsRemainingFuelPreferencesRepository,
    private val tyreTemperaturePreferencesRepository: AceWindowsTyreTemperaturePreferencesRepository,
    private val vehicleApproachPreferencesRepository: AceWindowsVehicleApproachPreferencesRepository,
) : AutoReadoutSettingsResetter {
    override suspend fun resetToDefault() {
        flagPreferencesRepository.resetToDefault()
        flagReadoutTextPreferencesRepository.resetToDefault()
        myBestLapPreferencesRepository.resetToDefault()
        remainingFuelLapsPreferencesRepository.resetToDefault()
        remainingFuelPreferencesRepository.resetToDefault()
        tyreTemperaturePreferencesRepository.resetToDefault()
        vehicleApproachPreferencesRepository.resetToDefault()
    }
}
