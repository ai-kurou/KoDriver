package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

class SaveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase(
    private val repository: LmuWindowsTyreTemperatureReadoutTextPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveOverheatReadoutText(text.normalizeReadoutCustomText())
    }
}
