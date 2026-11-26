package kurou.kodriver.core.acewindowsdata

import kurou.kodriver.domain.acewindows.repository.AceWindowsBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.autoreadout.repository.AutoReadoutSettingsResetter

// 自動読み上げ設定の各 DataStore を明示的に列挙する。
@Suppress("LongParameterList")
internal class AceWindowsAutoReadoutSettingsResetter(
    private val brakeTemperaturePreferencesRepository: AceWindowsBrakeTemperaturePreferencesRepository,
    private val flagPreferencesRepository: AceWindowsFlagPreferencesRepository,
    private val flagReadoutTextPreferencesRepository: AceWindowsFlagReadoutTextPreferencesRepository,
    private val myBestLapPreferencesRepository: AceWindowsMyBestLapPreferencesRepository,
    private val remainingFuelLapsPreferencesRepository: AceWindowsRemainingFuelLapsPreferencesRepository,
    private val remainingFuelPreferencesRepository: AceWindowsRemainingFuelPreferencesRepository,
    private val tyreTemperaturePreferencesRepository: AceWindowsTyreTemperaturePreferencesRepository,
    private val vehicleApproachPreferencesRepository: AceWindowsVehicleApproachPreferencesRepository,
) : AutoReadoutSettingsResetter {
    override suspend fun resetToDefault() {
        brakeTemperaturePreferencesRepository.resetToDefault()
        flagPreferencesRepository.resetToDefault()
        flagReadoutTextPreferencesRepository.resetToDefault()
        myBestLapPreferencesRepository.resetToDefault()
        remainingFuelLapsPreferencesRepository.resetToDefault()
        remainingFuelPreferencesRepository.resetToDefault()
        tyreTemperaturePreferencesRepository.resetToDefault()
        vehicleApproachPreferencesRepository.resetToDefault()
    }
}
