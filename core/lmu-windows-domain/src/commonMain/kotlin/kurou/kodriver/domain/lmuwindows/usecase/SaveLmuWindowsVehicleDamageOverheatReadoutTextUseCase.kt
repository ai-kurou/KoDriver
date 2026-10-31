package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

class SaveLmuWindowsVehicleDamageOverheatReadoutTextUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveOverheatReadoutText(text.normalizeReadoutCustomText())
    }
}
