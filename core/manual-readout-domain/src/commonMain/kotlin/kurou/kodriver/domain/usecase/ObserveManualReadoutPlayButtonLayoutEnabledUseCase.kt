package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository

public class ObserveManualReadoutPlayButtonLayoutEnabledUseCase(
    private val repository: ManualReadoutPlayButtonLayoutEnabledPreferencesRepository,
) {
    public operator fun invoke(): Flow<Boolean> = repository.observeManualReadoutPlayButtonLayoutEnabled()
}
