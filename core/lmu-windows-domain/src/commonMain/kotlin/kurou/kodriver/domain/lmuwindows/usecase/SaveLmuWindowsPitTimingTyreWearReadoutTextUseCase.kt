package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH

class SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase(
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveTyreWearReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
