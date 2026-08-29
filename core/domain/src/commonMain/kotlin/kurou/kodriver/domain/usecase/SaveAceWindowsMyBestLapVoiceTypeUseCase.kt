package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.AceWindowsMyBestLapPreferencesRepository

class SaveAceWindowsMyBestLapVoiceTypeUseCase(
    private val repository: AceWindowsMyBestLapPreferencesRepository,
) {
    suspend operator fun invoke(type: MyBestLapVoiceType) = repository.saveVoiceType(type)
}
