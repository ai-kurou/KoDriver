package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH

class SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase(
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveOverheatReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
