package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository

class ObserveLmuWindowsMyBestLapVoiceTypeUseCase(
    private val repository: LmuWindowsMyBestLapPreferencesRepository,
) {
    operator fun invoke(): Flow<MyBestLapVoiceType> = repository.observeVoiceType()
}
