package kurou.kodriver.domain.overlay.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.overlay.repository.OverlayTextColorPreferencesRepository

public class ObserveOverlayTextColorUseCase(
    private val repository: OverlayTextColorPreferencesRepository,
) {
    public operator fun invoke(): Flow<Long> = repository.observeOverlayTextColor()
}
