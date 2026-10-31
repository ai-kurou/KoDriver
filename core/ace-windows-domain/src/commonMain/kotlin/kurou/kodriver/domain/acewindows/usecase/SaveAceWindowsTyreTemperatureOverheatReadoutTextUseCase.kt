package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

class SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveOverheatReadoutText(text.normalizeReadoutCustomText())
    }
}
