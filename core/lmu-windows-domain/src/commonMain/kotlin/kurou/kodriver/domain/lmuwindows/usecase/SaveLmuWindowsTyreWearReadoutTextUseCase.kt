package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

class SaveLmuWindowsTyreWearReadoutTextUseCase(
    private val repository: LmuWindowsTyreWearPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.normalizeReadoutCustomText())
    }
}
