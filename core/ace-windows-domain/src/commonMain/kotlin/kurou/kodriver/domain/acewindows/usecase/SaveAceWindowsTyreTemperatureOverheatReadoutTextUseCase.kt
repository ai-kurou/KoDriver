package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

public class SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    public suspend operator fun invoke(text: String) {
        repository.saveOverheatReadoutText(text.normalizeReadoutCustomText())
    }
}
