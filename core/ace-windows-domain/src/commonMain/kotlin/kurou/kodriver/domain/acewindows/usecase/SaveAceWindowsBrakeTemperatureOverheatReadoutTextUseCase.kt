package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

public class SaveAceWindowsBrakeTemperatureOverheatReadoutTextUseCase(
    private val repository: AceWindowsBrakeTemperaturePreferencesRepository,
) {
    public suspend operator fun invoke(text: String) {
        repository.saveOverheatReadoutText(text.normalizeReadoutCustomText())
    }
}
