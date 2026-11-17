package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

public class SaveLmuWindowsPitTimingFuelImminentReadoutTextUseCase(
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository,
) {
    public suspend operator fun invoke(text: String) {
        repository.saveFuelImminentReadoutText(text.normalizeReadoutCustomText())
    }
}
