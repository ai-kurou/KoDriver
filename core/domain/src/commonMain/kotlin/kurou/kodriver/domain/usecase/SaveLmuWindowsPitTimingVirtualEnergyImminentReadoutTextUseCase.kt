package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository

class SaveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase(
    private val repository: LmuWindowsPitTimingPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveVirtualEnergyImminentReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
