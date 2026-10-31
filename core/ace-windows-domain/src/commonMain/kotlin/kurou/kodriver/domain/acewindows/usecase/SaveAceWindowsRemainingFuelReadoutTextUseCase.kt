package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

class SaveAceWindowsRemainingFuelReadoutTextUseCase(
    private val repository: AceWindowsRemainingFuelPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.normalizeReadoutCustomText())
    }
}
