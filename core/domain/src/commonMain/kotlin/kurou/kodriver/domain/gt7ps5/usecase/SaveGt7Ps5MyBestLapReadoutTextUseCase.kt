package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH

class SaveGt7Ps5MyBestLapReadoutTextUseCase(
    private val repository: Gt7Ps5MyBestLapPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
