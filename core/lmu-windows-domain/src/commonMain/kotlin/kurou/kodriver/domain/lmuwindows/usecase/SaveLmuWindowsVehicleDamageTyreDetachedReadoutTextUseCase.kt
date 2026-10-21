package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH

class SaveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveTyreDetachedReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
