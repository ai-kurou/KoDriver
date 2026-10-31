package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

class SaveLmuWindowsRemainingFuelReadoutTextUseCase(
    private val repository: LmuWindowsRemainingFuelPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.normalizeReadoutCustomText())
    }
}
