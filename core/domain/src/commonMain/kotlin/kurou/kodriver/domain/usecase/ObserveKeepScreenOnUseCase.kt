package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.KeepScreenOnPreferencesRepository

class ObserveKeepScreenOnUseCase(private val repository: KeepScreenOnPreferencesRepository) {
    operator fun invoke(): Flow<Boolean> = repository.keepScreenOn()
}
