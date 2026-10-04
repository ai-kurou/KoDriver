package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository

class SaveLmuWindowsBrakeTemperatureReadoutTextUseCase(
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
