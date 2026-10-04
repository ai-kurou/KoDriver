package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository

class SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase(
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveOverheatReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
