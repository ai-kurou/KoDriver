package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

public class SaveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    public suspend operator fun invoke(text: String) {
        repository.saveTyreDetachedReadoutText(text.normalizeReadoutCustomText())
    }
}
