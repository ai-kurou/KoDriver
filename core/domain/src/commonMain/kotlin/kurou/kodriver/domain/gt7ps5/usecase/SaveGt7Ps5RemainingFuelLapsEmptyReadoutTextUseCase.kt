package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH

class SaveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase(
    private val repository: Gt7Ps5RemainingFuelLapsPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveEmptyReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
