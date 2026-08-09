package kurou.kodriver.domain.usecase

import kurou.kodriver.core.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository

class SaveLmuWindowsMyBestLapVoiceTypeUseCase(
    private val repository: LmuWindowsMyBestLapPreferencesRepository,
) {
    suspend operator fun invoke(type: MyBestLapVoiceType) = repository.saveVoiceType(type)
}
