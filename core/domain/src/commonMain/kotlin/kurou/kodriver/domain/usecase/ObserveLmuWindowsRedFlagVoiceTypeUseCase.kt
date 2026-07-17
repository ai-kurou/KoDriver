package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository

class ObserveLmuWindowsRedFlagVoiceTypeUseCase(
    private val repository: LmuWindowsRedFlagPreferencesRepository,
) {
    operator fun invoke(): Flow<RedFlagVoiceType> = repository.observeVoiceType()
}
