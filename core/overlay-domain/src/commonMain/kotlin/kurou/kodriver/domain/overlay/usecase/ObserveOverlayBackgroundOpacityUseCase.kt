package kurou.kodriver.domain.overlay.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.overlay.repository.OverlayBackgroundOpacityPreferencesRepository

public class ObserveOverlayBackgroundOpacityUseCase(
    private val repository: OverlayBackgroundOpacityPreferencesRepository,
) {
    public operator fun invoke(): Flow<Int> = repository.observeOverlayBackgroundOpacity()
}
