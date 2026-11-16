package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

public class SaveAceWindowsRemainingFuelReadoutTextUseCase(
    private val repository: AceWindowsRemainingFuelPreferencesRepository,
) {
    public suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.normalizeReadoutCustomText())
    }
}
