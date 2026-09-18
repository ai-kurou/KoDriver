package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository

class ObserveOverlayVisibleUseCase(
    private val repository: OverlayVisiblePreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeOverlayVisible()
}
