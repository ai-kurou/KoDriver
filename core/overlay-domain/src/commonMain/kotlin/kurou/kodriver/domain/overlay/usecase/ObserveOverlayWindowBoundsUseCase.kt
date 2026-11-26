package kurou.kodriver.domain.overlay.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.overlay.model.OverlayWindowBounds
import kurou.kodriver.domain.overlay.repository.OverlayWindowBoundsPreferencesRepository

public class ObserveOverlayWindowBoundsUseCase(
    private val repository: OverlayWindowBoundsPreferencesRepository,
) {
    public operator fun invoke(): Flow<OverlayWindowBounds> = repository.observeOverlayWindowBounds()
}
