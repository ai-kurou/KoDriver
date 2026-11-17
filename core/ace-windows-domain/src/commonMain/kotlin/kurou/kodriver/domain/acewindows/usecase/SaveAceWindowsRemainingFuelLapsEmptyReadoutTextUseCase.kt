package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

public class SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase(
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository,
) {
    public suspend operator fun invoke(text: String) {
        repository.saveEmptyReadoutText(text.normalizeReadoutCustomText())
    }
}
