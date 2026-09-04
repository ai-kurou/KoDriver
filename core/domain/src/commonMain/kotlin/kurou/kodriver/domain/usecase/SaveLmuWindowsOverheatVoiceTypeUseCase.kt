package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.OverheatVoiceType
import kurou.kodriver.domain.repository.LmuWindowsOverheatPreferencesRepository

class SaveLmuWindowsOverheatVoiceTypeUseCase(
    private val repository: LmuWindowsOverheatPreferencesRepository,
) {
    suspend operator fun invoke(type: OverheatVoiceType) = repository.saveVoiceType(type)
}
