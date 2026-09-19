package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.OverlayWindowBounds
import kurou.kodriver.domain.repository.OverlayWindowBoundsPreferencesRepository

class ObserveOverlayWindowBoundsUseCase(
    private val repository: OverlayWindowBoundsPreferencesRepository,
) {
    operator fun invoke(): Flow<OverlayWindowBounds> = repository.observeOverlayWindowBounds()
}
