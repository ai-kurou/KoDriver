package kurou.kodriver.domain.overlay.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.overlay.repository.OverlayVisiblePreferencesRepository

public class ObserveOverlayVisibleUseCase(
    private val repository: OverlayVisiblePreferencesRepository,
) {
    public operator fun invoke(): Flow<Boolean> = repository.observeOverlayVisible()
}
