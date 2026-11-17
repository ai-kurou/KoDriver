package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository

public class ObserveOverlayBackgroundOpacityUseCase(
    private val repository: OverlayBackgroundOpacityPreferencesRepository,
) {
    public operator fun invoke(): Flow<Int> = repository.observeOverlayBackgroundOpacity()
}
