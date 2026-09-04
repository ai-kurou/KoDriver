package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.OverheatVoiceType
import kurou.kodriver.domain.repository.LmuWindowsOverheatPreferencesRepository

class ObserveLmuWindowsOverheatVoiceTypeUseCase(
    private val repository: LmuWindowsOverheatPreferencesRepository,
) {
    operator fun invoke(): Flow<OverheatVoiceType> = repository.observeVoiceType()
}
