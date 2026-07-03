package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository

class SaveGt7Ps5MyBestLapVoiceTypeUseCase(
    private val repository: Gt7Ps5MyBestLapPreferencesRepository,
) {
    suspend operator fun invoke(type: MyBestLapVoiceType) = repository.saveVoiceType(type)
}
