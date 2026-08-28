package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.AceWindowsMyBestLapPreferencesRepository

class ObserveAceWindowsMyBestLapVoiceTypeUseCase(
    private val repository: AceWindowsMyBestLapPreferencesRepository,
) {
    operator fun invoke(): Flow<MyBestLapVoiceType> = repository.observeVoiceType()
}
