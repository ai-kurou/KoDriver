package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository

class ObserveOverlayTextColorUseCase(
    private val repository: OverlayTextColorPreferencesRepository,
) {
    operator fun invoke(): Flow<Long> = repository.observeOverlayTextColor()
}
