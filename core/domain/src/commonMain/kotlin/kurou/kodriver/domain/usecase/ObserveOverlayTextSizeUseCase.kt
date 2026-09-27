package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository

class ObserveOverlayTextSizeUseCase(
    private val repository: OverlayTextSizePreferencesRepository,
) {
    operator fun invoke(): Flow<OverlayTextSize> = repository.observeOverlayTextSize()
}
