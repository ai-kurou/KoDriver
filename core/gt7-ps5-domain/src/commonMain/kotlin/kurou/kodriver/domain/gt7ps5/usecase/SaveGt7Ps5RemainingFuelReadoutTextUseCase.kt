package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

class SaveGt7Ps5RemainingFuelReadoutTextUseCase(
    private val repository: Gt7Ps5RemainingFuelPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.normalizeReadoutCustomText())
    }
}
