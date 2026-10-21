package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH

class SaveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase(
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveVirtualEnergyImminentReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
