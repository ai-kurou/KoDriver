package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository

class SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveTyreWearImminentReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
