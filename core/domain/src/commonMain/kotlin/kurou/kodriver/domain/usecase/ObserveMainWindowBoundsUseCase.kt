package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.MainWindowBounds
import kurou.kodriver.domain.repository.MainWindowBoundsPreferencesRepository

class ObserveMainWindowBoundsUseCase(
    private val repository: MainWindowBoundsPreferencesRepository,
) {
    operator fun invoke(): Flow<MainWindowBounds> = repository.observeMainWindowBounds()
}
