package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

public class SaveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase(
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository,
) {
    public suspend operator fun invoke(text: String) {
        repository.saveVirtualEnergyImminentReadoutText(text.normalizeReadoutCustomText())
    }
}
