package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository

class SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase(
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveTyreWearReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
