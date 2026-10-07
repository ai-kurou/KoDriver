package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository

class SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveOverheatReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
