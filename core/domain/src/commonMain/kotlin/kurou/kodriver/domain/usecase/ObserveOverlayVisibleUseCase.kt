package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository

public class ObserveOverlayVisibleUseCase(
    private val repository: OverlayVisiblePreferencesRepository,
) {
    public operator fun invoke(): Flow<Boolean> = repository.observeOverlayVisible()
}
