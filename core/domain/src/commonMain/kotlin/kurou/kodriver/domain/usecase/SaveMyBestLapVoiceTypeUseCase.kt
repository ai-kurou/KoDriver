package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.MyBestLapPreferencesRepository

class SaveMyBestLapVoiceTypeUseCase(
    private val repository: MyBestLapPreferencesRepository,
) {
    suspend operator fun invoke(type: MyBestLapVoiceType) = repository.saveVoiceType(type)
}
