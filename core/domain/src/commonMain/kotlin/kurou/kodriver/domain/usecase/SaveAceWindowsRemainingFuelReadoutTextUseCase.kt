package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository

class SaveAceWindowsRemainingFuelReadoutTextUseCase(
    private val repository: AceWindowsRemainingFuelPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
