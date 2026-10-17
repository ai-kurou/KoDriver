package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_FLAG_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

class ObserveAceWindowsFlagEnabledStatesUseCase(
    private val repository: AceWindowsFlagPreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeFlagEnabledStates().map { persisted -> ACE_WINDOWS_FLAG_ENABLED_STATE_DEFAULT + persisted }
}
