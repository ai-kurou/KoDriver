package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.MyBestLapPreferencesRepository

class ObserveMyBestLapVoiceTypeUseCase(
    private val repository: MyBestLapPreferencesRepository,
) {
    operator fun invoke(): Flow<MyBestLapVoiceType> = repository.observeVoiceType()
}
