package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.domain.model.ACE_WINDOWS_FLAG_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository

class ObserveAceWindowsFlagEnabledStatesUseCase(
    private val repository: AceWindowsFlagPreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeFlagEnabledStates().map { persisted -> ACE_WINDOWS_FLAG_ENABLED_STATE_DEFAULT + persisted }
}
