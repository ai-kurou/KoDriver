package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository

class SaveLmuWindowsRedFlagVoiceTypeUseCase(
    private val repository: LmuWindowsRedFlagPreferencesRepository,
) {
    suspend operator fun invoke(type: RedFlagVoiceType) = repository.saveVoiceType(type)
}
