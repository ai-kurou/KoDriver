package kurou.kodriver.core.gt7ps5data

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.AutoReadoutSettingsResetter

internal class Gt7Ps5AutoReadoutSettingsResetter(
    private val myBestLapPreferencesRepository: Gt7Ps5MyBestLapPreferencesRepository,
    private val remainingFuelLapsPreferencesRepository: Gt7Ps5RemainingFuelLapsPreferencesRepository,
    private val remainingFuelPreferencesRepository: Gt7Ps5RemainingFuelPreferencesRepository,
    private val tyreTemperaturePreferencesRepository: Gt7Ps5TyreTemperaturePreferencesRepository,
) : AutoReadoutSettingsResetter {
    override suspend fun resetToDefault() {
        myBestLapPreferencesRepository.resetToDefault()
        remainingFuelLapsPreferencesRepository.resetToDefault()
        remainingFuelPreferencesRepository.resetToDefault()
        tyreTemperaturePreferencesRepository.resetToDefault()
    }
}
