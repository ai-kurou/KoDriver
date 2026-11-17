package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

public class SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    public suspend operator fun invoke(text: String) {
        repository.savePartDetachedReadoutText(text.normalizeReadoutCustomText())
    }
}
