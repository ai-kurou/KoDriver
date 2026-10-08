package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository

class SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase(
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveTyreWearImminentReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
