package kurou.kodriver.domain.overlay.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.overlay.model.OverlayTextSize
import kurou.kodriver.domain.overlay.repository.OverlayTextSizePreferencesRepository

public class ObserveOverlayTextSizeUseCase(
    private val repository: OverlayTextSizePreferencesRepository,
) {
    public operator fun invoke(): Flow<OverlayTextSize> = repository.observeOverlayTextSize()
}
