package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository

class ObserveGt7Ps5MyBestLapVoiceTypeUseCase(
    private val repository: Gt7Ps5MyBestLapPreferencesRepository,
) {
    operator fun invoke(): Flow<MyBestLapVoiceType> = repository.observeVoiceType()
}
