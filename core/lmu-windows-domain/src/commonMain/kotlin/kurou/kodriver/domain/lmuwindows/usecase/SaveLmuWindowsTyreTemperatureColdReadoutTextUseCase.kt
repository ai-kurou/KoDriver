package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

public class SaveLmuWindowsTyreTemperatureColdReadoutTextUseCase(
    private val repository: LmuWindowsTyreTemperatureReadoutTextPreferencesRepository,
) {
    public suspend operator fun invoke(text: String) {
        repository.saveColdReadoutText(text.normalizeReadoutCustomText())
    }
}
