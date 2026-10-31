package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

class SaveAceWindowsMyBestLapReadoutTextUseCase(
    private val repository: AceWindowsMyBestLapPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.normalizeReadoutCustomText())
    }
}
