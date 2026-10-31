package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

class SaveLmuWindowsMyBestLapReadoutTextUseCase(
    private val repository: LmuWindowsMyBestLapPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.normalizeReadoutCustomText())
    }
}
