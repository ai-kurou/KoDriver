package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository

class ObserveManualReadoutPlayButtonLayoutEnabledUseCase(
    private val repository: ManualReadoutPlayButtonLayoutEnabledPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeManualReadoutPlayButtonLayoutEnabled()
}
