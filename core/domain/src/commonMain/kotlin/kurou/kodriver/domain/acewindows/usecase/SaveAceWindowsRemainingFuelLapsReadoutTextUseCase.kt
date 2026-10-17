package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH

class SaveAceWindowsRemainingFuelLapsReadoutTextUseCase(
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
