package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

public class SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase(
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository,
) {
    public suspend operator fun invoke(text: String) {
        repository.saveOverheatReadoutText(text.normalizeReadoutCustomText())
    }
}
