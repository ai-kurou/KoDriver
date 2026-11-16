package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

public class SaveAceWindowsRemainingFuelLapsReadoutTextUseCase(
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository,
) {
    public suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.normalizeReadoutCustomText())
    }
}
