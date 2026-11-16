package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

public class SaveAceWindowsMyBestLapReadoutTextUseCase(
    private val repository: AceWindowsMyBestLapPreferencesRepository,
) {
    public suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.normalizeReadoutCustomText())
    }
}
