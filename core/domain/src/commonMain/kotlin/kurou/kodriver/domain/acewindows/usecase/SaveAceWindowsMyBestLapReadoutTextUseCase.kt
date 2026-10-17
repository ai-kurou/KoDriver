package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH

class SaveAceWindowsMyBestLapReadoutTextUseCase(
    private val repository: AceWindowsMyBestLapPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
