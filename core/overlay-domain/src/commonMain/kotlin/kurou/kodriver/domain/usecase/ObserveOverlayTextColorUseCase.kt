package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository

public class ObserveOverlayTextColorUseCase(
    private val repository: OverlayTextColorPreferencesRepository,
) {
    public operator fun invoke(): Flow<Long> = repository.observeOverlayTextColor()
}
