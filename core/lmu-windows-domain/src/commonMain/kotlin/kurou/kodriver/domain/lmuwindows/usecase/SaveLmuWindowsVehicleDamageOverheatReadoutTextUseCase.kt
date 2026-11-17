package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

public class SaveLmuWindowsVehicleDamageOverheatReadoutTextUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    public suspend operator fun invoke(text: String) {
        repository.saveOverheatReadoutText(text.normalizeReadoutCustomText())
    }
}
